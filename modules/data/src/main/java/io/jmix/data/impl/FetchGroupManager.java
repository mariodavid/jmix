/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.Entity;
import io.jmix.core.entity.SoftDelete;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.jpa.JpaQuery;
import org.eclipse.persistence.queries.AttributeGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.LoadGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component(FetchGroupManager.NAME)
public class FetchGroupManager {

    public static final String NAME = "data_FetchGroupManager";

    private final Logger log = LoggerFactory.getLogger(FetchGroupManager.class);

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private FetchPlanRepository viewRepository;

    @Autowired
    private ExtendedEntities extendedEntities;

    public void setFetchPlan(JpaQuery query, String queryString, @Nullable FetchPlan fetchPlan, boolean singleResultExpected) {
        Preconditions.checkNotNullArgument(query, "query is null");
        if (fetchPlan != null) {
            AttributeGroup ag = fetchPlan.loadPartialEntities() ? new FetchGroup() : new LoadGroup();
            applyFetchPlan(query, queryString, ag, fetchPlan, singleResultExpected);
        } else {
            query.setHint(QueryHints.FETCH_GROUP, null);
        }
    }

    public void addFetchPlan(JpaQuery query, String queryString, FetchPlan fetchPlan, boolean singleResultExpected) {
        Preconditions.checkNotNullArgument(query, "query is null");
        Preconditions.checkNotNullArgument(fetchPlan, "fetch plan is null");

        Map<String, Object> hints = query.getHints();
        AttributeGroup ag = null;
        if (fetchPlan.loadPartialEntities()) {
            if (hints != null)
                ag = (FetchGroup) hints.get(QueryHints.FETCH_GROUP);
            if (ag == null)
                ag = new FetchGroup();
        } else {
            if (hints != null)
                ag = (LoadGroup) hints.get(QueryHints.LOAD_GROUP);
            if (ag == null)
                ag = new LoadGroup();
        }

        applyFetchPlan(query, queryString, ag, fetchPlan, singleResultExpected);
    }

    private void applyFetchPlan(JpaQuery query, String queryString, AttributeGroup attrGroup, FetchPlan fetchPlan,
                                boolean singleResultExpected) {

        boolean useFetchGroup = attrGroup instanceof FetchGroup;

        FetchGroupDescription description = calculateFetchGroup(queryString, fetchPlan, singleResultExpected, useFetchGroup);

        if (attrGroup instanceof FetchGroup)
            ((FetchGroup) attrGroup).setShouldLoadAll(true);

        if (log.isTraceEnabled())
            log.trace((useFetchGroup ? "Fetch" : "Load") + " group for " + fetchPlan + ":\n" + description.getAttributes().stream().collect(Collectors.joining("\n")));
        for (String attribute : description.getAttributes()) {
            attrGroup.addAttribute(attribute);
        }

        MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        if (!metadataTools.isCacheable(metaClass)) {
            query.setHint(useFetchGroup ? QueryHints.FETCH_GROUP : QueryHints.LOAD_GROUP, attrGroup);
        }

        if (log.isDebugEnabled()) {
            String fetchModes = description.getHints().entrySet().stream()
                    .map(e -> e.getKey() + "=" + (e.getValue().equals(QueryHints.LEFT_FETCH) ? "JOIN" : "BATCH"))
                    .collect(Collectors.joining(", "));
            log.debug("Fetch modes for " + fetchPlan + ": " + (fetchModes.equals("") ? "<none>" : fetchModes));
        }

        for (Map.Entry<String, String> entry : description.getHints().entrySet()) {
            query.setHint(entry.getValue(), entry.getKey());
        }

        if (description.hasBatches()) {
            query.setHint(QueryHints.BATCH_TYPE, "IN");
        }
    }

    public FetchGroupDescription calculateFetchGroup(String queryString,
                                                     FetchPlan fetchPlan,
                                                     boolean singleResultExpected,
                                                     boolean useFetchGroup) {
        Set<FetchGroupField> fetchGroupFields = new LinkedHashSet<>();
        processFetchPlan(fetchPlan, null, fetchGroupFields, useFetchGroup);

        FetchGroupDescription description = new FetchGroupDescription();

        for (FetchGroupField field : fetchGroupFields) {
            description.addAttribute(field.path());
        }

        List<FetchGroupField> refFields = new ArrayList<>();
        for (FetchGroupField field : fetchGroupFields) {
            if (field.metaProperty.getRange().isClass() && !metadataTools.isEmbedded(field.metaProperty))
                refFields.add(field);
        }

        MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        if (!refFields.isEmpty()) {
            String alias = QueryTransformerFactory.createParser(queryString).getEntityAlias();

            List<FetchGroupField> batchFields = new ArrayList<>();
            List<FetchGroupField> joinFields = new ArrayList<>();

            for (FetchGroupField refField : refFields) {
                if (refField.fetchMode == FetchMode.UNDEFINED) {
                    if (refField.metaProperty.getRange().getCardinality().isMany()) {
                        List<String> masterAttributes = getMasterEntityAttributes(fetchGroupFields, refField, useFetchGroup);
                        description.addAttributes(masterAttributes);
                    }
                    continue;
                }

                boolean selfRef = false;
                for (MetaProperty mp : refField.metaPropertyPath.getMetaProperties()) {
                    if (!mp.getRange().getCardinality().isMany()) {
                        MetaClass mpClass = mp.getRange().asClass();
                        if (metadataTools.isAssignableFrom(mpClass, metaClass) || metadataTools.isAssignableFrom(metaClass, mpClass)) {
                            batchFields.add(refField);
                            selfRef = true;
                            break;
                        }
                    }
                }

                if (!selfRef) {
                    if (refField.metaProperty.getRange().getCardinality().isMany()) {
                        List<String> masterAttributes = getMasterEntityAttributes(fetchGroupFields, refField, useFetchGroup);
                        description.addAttributes(masterAttributes);

                        if (refField.fetchMode == FetchMode.JOIN) {
                            joinFields.add(refField);
                        } else {
                            batchFields.add(refField);
                        }
                    } else {
                        if (refField.fetchMode == FetchMode.BATCH) {
                            batchFields.add(refField);
                        } else {
                            joinFields.add(refField);
                        }
                    }
                }
            }

            for (FetchGroupField joinField : new ArrayList<>(joinFields)) {
                // adjust fetch mode according to parent attributes
                if (joinField.fetchMode == FetchMode.AUTO) {
                    Optional<FetchMode> parentMode = refFields.stream()
                            .filter(f -> joinField.metaPropertyPath.startsWith(f.metaPropertyPath) && joinField.fetchMode != FetchMode.JOIN)
                            .sorted((f1, f2) -> f1.metaPropertyPath.getPath().length - f2.metaPropertyPath.getPath().length)
                            .findFirst()
                            .map(f -> f.fetchMode);
                    if (parentMode.isPresent() && parentMode.get() == FetchMode.UNDEFINED) {
                        joinFields.remove(joinField);
                    } else {
                        for (FetchGroupField batchField : new ArrayList<>(batchFields)) {
                            if (joinField.metaPropertyPath.startsWith(batchField.metaPropertyPath)) {
                                joinFields.remove(joinField);
                                batchFields.add(joinField);
                            }
                        }
                    }
                }
            }

            QueryParser parser = QueryTransformerFactory.createParser(queryString);

            List<FetchGroupField> isNullFields = joinFields.stream()
                    .filter(f -> f.fetchMode == FetchMode.AUTO &&
                            (parser.hasIsNullCondition(f.path()) || parser.hasIsNotNullCondition(f.path())))
                    .collect(Collectors.toList());
            if (!isNullFields.isEmpty()) {
                for (Iterator<FetchGroupField> fieldIt = joinFields.iterator(); fieldIt.hasNext(); ) {
                    FetchGroupField joinField = fieldIt.next();
                    boolean isNullField = isNullFields.stream()
                            .anyMatch(f -> joinField == f || f.fetchMode == FetchMode.AUTO
                                    && joinField.metaPropertyPath.startsWith(f.metaPropertyPath));
                    if (isNullField) {
                        fieldIt.remove();
                        description.removeAttributeIf(attr -> attr.startsWith(joinField.path() + "."));
                    }
                }
            }


            long toManyCount = refFields.stream()
                    .filter(f -> f.metaProperty.getRange().getCardinality().isMany()).count();

            // For query by ID, remove BATCH mode for to-many attributes that have no nested attributes
            if (singleResultExpected && toManyCount <= 1) {
                for (FetchGroupField batchField : new ArrayList<>(batchFields)) {
                    if (batchField.metaProperty.getRange().getCardinality().isMany()) {
                        boolean hasNested = refFields.stream()
                                .anyMatch(f -> f != batchField && f.metaPropertyPath.startsWith(batchField.metaPropertyPath));
                        if (!hasNested && batchField.fetchMode != FetchMode.BATCH) {
                            batchFields.remove(batchField);
                        }
                    }
                }
            }

            //Remove this fields from BATCH processing
            for (FetchGroupField refField : refFields) {
                //Find many-to-many fields with cycle loading same: {E}.b.a.b, where a of type {E}.
                //If {E}.b BATCH, {E}.b.a BATCH and {E}.b.a.b BATCH then same query used simultaneously
                //while loading {E}.b and {E}.b.a.b, so result of batch query is incorrect.
                if (refField.fetchMode == FetchMode.AUTO &&
                        refField.metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_MANY) {
                    //find property {E}.a.b for {E}.a where b of type {E}
                    List<FetchGroupField> selfRefs = refFields.stream()
                            .filter(f -> isTransitiveSelfReference(refField, f, Range.Cardinality.MANY_TO_MANY, refField.metaClass))
                            .collect(Collectors.toList());
                    for (FetchGroupField selfRef : selfRefs) {
                        List<FetchGroupField> secondLevelSelfRefs = refFields.stream()
                                .filter(f -> isTransitiveSelfReference(selfRef, f, Range.Cardinality.MANY_TO_MANY, selfRef.metaClass))
                                .collect(Collectors.toList());
                        for (FetchGroupField f : secondLevelSelfRefs) {
                            batchFields.remove(f);
                            batchFields.remove(selfRef);
                            batchFields.remove(refField);
                        }
                    }
                }

                if (refField.fetchMode == FetchMode.AUTO &&
                        refField.metaProperty.getRange().getCardinality() == Range.Cardinality.ONE_TO_MANY) {
                    //find properties {E}.a.b.a for {E}.a
                    List<FetchGroupField> selfRefs = refFields.stream()
                            .filter(f -> isTransitiveSelfReference(refField, f, Range.Cardinality.ONE_TO_MANY, refField.metaProperty.getRange().asClass()))
                            .collect(Collectors.toList());
                    //check if {E} is same as b
                    if (!selfRefs.isEmpty()) {
                        selfRefs.addAll(refFields.stream()
                                .filter(f -> isTransitiveSelfReference(refField, f, Range.Cardinality.MANY_TO_ONE, metaClass))
                                .collect(Collectors.toList()));
                    }
                    for (FetchGroupField selfRef : selfRefs) {
                        batchFields.remove(selfRef);
                        batchFields.remove(refField);
                    }
                }

                //remove BATCH fields for cached classes
                if (refField.fetchMode == FetchMode.UNDEFINED && refField.cacheable) {
                    for (FetchGroupField batchField : new ArrayList<>(batchFields)) {
                        if (batchField != refField && batchField.metaPropertyPath.startsWith(refField.metaPropertyPath)) {
                            batchFields.remove(batchField);
                        }
                    }
                }
            }

            for (FetchGroupField joinField : joinFields) {
                String attr = alias + "." + joinField.path();
                description.addHint(attr, QueryHints.LEFT_FETCH);
            }

            for (FetchGroupField batchField : batchFields) {
                if (batchField.fetchMode == FetchMode.BATCH || !singleResultExpected || batchFields.size() > 1) {
                    String attr = alias + "." + batchField.path();
                    description.addHint(attr, QueryHints.BATCH);
                }
            }
        }

        return description;
    }

    private boolean isTransitiveSelfReference(FetchGroupField root, FetchGroupField current,
                                              Range.Cardinality cardinality, MetaClass metaClass) {
        return root != current
                && current.fetchMode == FetchMode.AUTO
                && current.metaPropertyPath.startsWith(root.metaPropertyPath)
                && current.metaProperty.getRange().isClass()
                && current.metaProperty.getRange().getCardinality() == cardinality
                && Objects.equals(current.metaProperty.getRange().asClass(), metaClass);
    }

    private List<String> getMasterEntityAttributes(Set<FetchGroupField> fetchGroupFields,
                                                   FetchGroupField toManyField, boolean useFetchGroup) {
        List<String> result = new ArrayList<>();

        MetaClass propMetaClass = toManyField.metaProperty.getRange().asClass();
        propMetaClass.getProperties().stream()
                .filter(mp -> mp.getRange().isClass() && toManyField.metaProperty.getInverse() == mp)
                .findFirst()
                .ifPresent(inverseProp -> {
                    if (useFetchGroup) {
                        for (FetchGroupField fetchGroupField : fetchGroupFields) {
                            // compare with original class, because in case of entity extension properties are remapped to extended entities
                            MetaClass inversePropRangeClass = extendedEntities.getOriginalOrThisMetaClass(
                                    inverseProp.getRange().asClass());
                            if (fetchGroupField.metaClass.equals(toManyField.metaClass)
                                    // add only local properties
                                    && !fetchGroupField.metaProperty.getRange().isClass()
                                    // do not add properties from subclasses
                                    && fetchGroupField.metaProperty.getDomain().equals(inversePropRangeClass)) {
                                String attribute = toManyField.path() + "." + inverseProp.getName() + "." + fetchGroupField.metaProperty.getName();
                                result.add(attribute);
                            }
                        }
                        if (result.isEmpty()) {
                            result.add(toManyField.path() + "." + inverseProp.getName() + "."
                                    + metadataTools.getPrimaryKeyName(inverseProp.getDomain()));
                        }
                    } else {
                        result.add(toManyField.path() + "." + inverseProp.getName());
                    }
                });

        return result;
    }

    private void processFetchPlan(FetchPlan fetchPlan, FetchGroupField parentField, Set<FetchGroupField> fetchGroupFields, boolean useFetchGroup) {
        Class<? extends Entity> entityClass = fetchPlan.getEntityClass();
        MetaClass entityMetaClass = metadata.getClass(entityClass);

        if (useFetchGroup) {
            // Always add SoftDelete properties to support EntityManager contract
            if (SoftDelete.class.isAssignableFrom(entityClass)) {
                for (String property : getInterfaceProperties(SoftDelete.class)) {
                    fetchGroupFields.add(createFetchGroupField(entityClass, parentField, property));
                }
            }

            // Always add uuid property if the entity has primary key not of type UUID
            MetaProperty pkProperty = metadataTools.getPrimaryKeyProperty(entityMetaClass);
            if (pkProperty != null && !UUID.class.equals(pkProperty.getJavaType())) {
                MetaProperty uuidProp = entityMetaClass.findProperty("uuid");
                if (uuidProp != null && metadataTools.isPersistent(uuidProp)) {
                    fetchGroupFields.add(createFetchGroupField(entityClass, parentField, "uuid"));
                }
            }
        }

        for (FetchPlanProperty property : fetchPlan.getProperties()) {
            String propertyName = property.getName();
            MetaProperty metaProperty = entityMetaClass.getProperty(propertyName);

            if (metadataTools.isPersistent(metaProperty) && (metaProperty.getRange().isClass() || useFetchGroup)) {
                FetchGroupField field = createFetchGroupField(entityClass, parentField, propertyName, property.getFetchMode());
                fetchGroupFields.add(field);
                if (property.getFetchPlan() != null) {
                    if (ClassUtils.isPrimitiveOrWrapper(metaProperty.getJavaType()) ||
                            String.class.isAssignableFrom(metaProperty.getJavaType())) {
                        String message = "Wrong fetch plans mechanism usage found. Fetch plan %s is set for property \"%s\" of " +
                                "class \"%s\", but this property does not point to an Entity";

                        String propertyFetchPlanName = property.getFetchPlan().getName();
                        propertyFetchPlanName = propertyFetchPlanName != null && !propertyFetchPlanName.isEmpty()
                                ? "\"" + propertyFetchPlanName + "\""
                                : "";

                        message = String.format(message, propertyFetchPlanName, property.getName(),
                                entityMetaClass.getName());
                        throw new DevelopmentException(message);
                    }

                    processFetchPlan(property.getFetchPlan(), field, fetchGroupFields, useFetchGroup);
                }
            }

            List<String> relatedProperties = metadataTools.getRelatedProperties(entityClass, propertyName);
            for (String relatedProperty : relatedProperties) {
                MetaProperty relatedMetaProp = entityMetaClass.getProperty(relatedProperty);
                if (!fetchPlan.containsProperty(relatedProperty) && (relatedMetaProp.getRange().isClass() || useFetchGroup)) {
                    FetchGroupField field = createFetchGroupField(entityClass, parentField, relatedProperty);
                    fetchGroupFields.add(field);
                    if (relatedMetaProp.getRange().isClass()) {
                        FetchPlan relatedFetchPlan = viewRepository.getFetchPlan(relatedMetaProp.getRange().asClass(), FetchPlan.MINIMAL);
                        processFetchPlan(relatedFetchPlan, field, fetchGroupFields, useFetchGroup);
                    }
                }
            }
        }
    }

    private List<String> getInterfaceProperties(Class<?> intf) {
        List<String> result = new ArrayList<>();
        for (Method method : intf.getDeclaredMethods()) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                result.add(StringUtils.uncapitalize(method.getName().substring(3)));
            }
        }
        return result;
    }

    private FetchGroupField createFetchGroupField(Class<? extends Entity> entityClass,
                                                  FetchGroupField parentField,
                                                  String property) {
        return createFetchGroupField(entityClass, parentField, property, FetchMode.AUTO);
    }

    private FetchGroupField createFetchGroupField(Class<? extends Entity> entityClass,
                                                  FetchGroupField parentField,
                                                  String property,
                                                  FetchMode fetchMode) {
        MetaClass metaClass = metadata.getClass(entityClass);

        MetaProperty metaProperty = metaClass.getProperty(property);
        MetaClass fetchMetaClass = metaProperty.getRange().isClass() ? metaProperty.getRange().asClass() : metaClass;

        return new FetchGroupField(metaClass, parentField, property, getFetchMode(fetchMetaClass, fetchMode), metadataTools.isCacheable(metaClass));
    }

    private FetchMode getFetchMode(MetaClass metaClass, FetchMode fetchMode) {
        return metadataTools.isCacheable(metaClass) ? FetchMode.UNDEFINED : fetchMode;
    }

    protected static class FetchGroupField {
        private final MetaClass metaClass;
        private FetchMode fetchMode;
        private final MetaProperty metaProperty;
        private final MetaPropertyPath metaPropertyPath;
        private final boolean cacheable;

        public FetchGroupField(MetaClass metaClass, FetchGroupField parentField, String property, FetchMode fetchMode, boolean cacheable) {
            this.metaClass = metaClass;
            this.fetchMode = fetchMode;
            this.metaProperty = metaClass.getProperty(property);
            this.metaPropertyPath = parentField == null ?
                    new MetaPropertyPath(metaClass, metaProperty) :
                    new MetaPropertyPath(parentField.metaPropertyPath, metaProperty);
            this.cacheable = cacheable;
        }

        public String path() {
            return metaPropertyPath.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FetchGroupField that = (FetchGroupField) o;

            if (!metaClass.equals(that.metaClass)) return false;
            if (!metaPropertyPath.equals(that.metaPropertyPath)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = metaClass.hashCode();
            result = 31 * result + metaPropertyPath.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return path();
        }
    }
}
