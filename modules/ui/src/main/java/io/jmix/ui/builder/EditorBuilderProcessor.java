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

package io.jmix.ui.builder;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.HasValueSource;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityOptions;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.Nested;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;

@Component("ui_EditorBuilderProcessor")
public class EditorBuilderProcessor {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected WindowConfig windowConfig;
    @Autowired
    protected UiProperties properties;
    @Autowired
    protected List<EditedEntityTransformer> editedEntityTransformers;

    @SuppressWarnings("unchecked")
    public  <E extends Entity, S extends Screen> S buildEditor(EditorBuilder<E> builder) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = getScreenContext(origin).getScreens();

        ListComponent<E> listComponent = builder.getListComponent();

        CollectionContainer<E> container = builder.getContainer();

        if (container == null && listComponent != null) {
            DataUnit items = listComponent.getItems();
            container = items instanceof ContainerDataUnit ? ((ContainerDataUnit) items).getContainer() : null;
        }

        E entity = initEntity(builder, container);

        if (builder.getMode() == EditMode.EDIT && entity == null) {
            throw new IllegalStateException(String.format("Editor of %s cannot be open with mode EDIT, entity is not set",
                    builder.getEntityClass()));
        }

        Screen screen = createScreen(builder, screens, entity);

        EditorScreen<E> editorScreen = (EditorScreen<E>) screen;
        editorScreen.setEntityToEdit(entity);

        DataContext parentDataContext = setupParentDataContext(
                origin, screen, container, builder.getParentDataContext());

        if (container != null) {
            CollectionContainer<E> ct = container;
            screen.addAfterCloseListener(event -> {
                CloseAction closeAction = event.getCloseAction();
                if (isCommitCloseAction(closeAction)) {
                    E entityFromEditor = getCommittedEntity(editorScreen, parentDataContext);
                    E reloadedEntity = transformForCollectionContainer(entityFromEditor, ct);
                    E committedEntity = transform(reloadedEntity, builder);

                    if (builder.getMode() == EditMode.CREATE) {
                        boolean addsFirst;

                        if (!(ct instanceof Nested)) {
                            addsFirst = properties.isCreateActionAddsFirst();
                            if (builder.getAddFirst() != null) {
                                addsFirst = builder.getAddFirst();
                            }
                        } else {
                            addsFirst = false;
                        }

                        if (ct instanceof Nested || !addsFirst) {
                            ct.getMutableItems().add(committedEntity);
                        } else {
                            ct.getMutableItems().add(0, committedEntity);
                        }
                    } else {
                        ct.replaceItem(committedEntity);
                    }
                }
                if (listComponent instanceof io.jmix.ui.component.Component.Focusable) {
                    ((io.jmix.ui.component.Component.Focusable) listComponent).focus();
                }
            });
        }

        HasValue<E> field = builder.getField();
        if (field != null) {

            if (parentDataContext == null && field instanceof HasValueSource) {
                ValueSource fieldValueSource = ((HasValueSource) field).getValueSource();
                if (fieldValueSource instanceof EntityValueSource) {
                    if (isCompositionProperty((EntityValueSource) fieldValueSource)) {
                        DataContext thisDataContext = UiControllerUtils.getScreenData(origin).getDataContext();
                        UiControllerUtils.getScreenData(screen).getDataContext().setParent(thisDataContext);
                    }
                }
            }

            screen.addAfterCloseListener(event -> {
                CloseAction closeAction = event.getCloseAction();
                if (isCommitCloseAction(closeAction)) {
                    E entityFromEditor = editorScreen.getEditedEntity();
                    E reloadedEntity = transformForField(entityFromEditor, field);
                    E editedEntity = transform(reloadedEntity, builder);

                    if (field instanceof EntityComboBox) {
                        EntityComboBox entityComboBox = ((EntityComboBox) field);
                        Options options = entityComboBox.getOptions();
                        if (options instanceof EntityOptions) {
                            EntityOptions entityOptions = (EntityOptions) options;
                            if (entityOptions.containsItem(editedEntity)) {
                                entityOptions.updateItem(editedEntity);
                            }
                        }
                    }

                    if (field instanceof SupportsUserAction) {
                        ((SupportsUserAction) field).setValueFromUser(editedEntity);
                    } else {
                        field.setValue(editedEntity);
                    }
                }

                if (field instanceof io.jmix.ui.component.Component.Focusable) {
                    ((io.jmix.ui.component.Component.Focusable) field).focus();
                }
            });
        }

        if (builder instanceof EditorClassBuilder) {
            @SuppressWarnings("unchecked")
            Consumer<AfterScreenCloseEvent> closeListener = ((EditorClassBuilder) builder).getCloseListener();
            if (closeListener != null) {
                screen.addAfterCloseListener(new AfterCloseListenerAdapter(closeListener));
            }
        }

        return (S) screen;
    }

    protected  <E extends Entity> E transform(E entity, EditorBuilder<E> builder) {
        if (builder.getTransformation() != null) {
            return builder.getTransformation().apply(entity);
        }
        return entity;
    }

    protected  <E extends Entity> E transformForCollectionContainer(E entity, CollectionContainer<E> container) {
        E result = entity;
        for (EditedEntityTransformer transformer : editedEntityTransformers) {
            result = transformer.transformForCollectionContainer(result, container);
        }
        return result;
    }

    protected <E extends Entity> E transformForField(E entity, HasValue<E> field) {
        E result = entity;
        for (EditedEntityTransformer transformer : editedEntityTransformers) {
            result = transformer.transformForField(result, field);
        }
        return result;
    }

    protected <E extends Entity> E getCommittedEntity(EditorScreen<E> editorScreen, @Nullable DataContext parentDataContext) {
        E editedEntity = editorScreen.getEditedEntity();
        if (parentDataContext != null) {
            E trackedEntity = parentDataContext.find(editedEntity);
            if (trackedEntity != null) { // makes sense for NoopDataContext
                return trackedEntity;
            }
        }
        return editedEntity;
    }

    protected <E extends Entity> E initEntity(EditorBuilder<E> builder, CollectionContainer<E> container) {
        E entity;

        boolean oneToOneComposition = false;
        EntityValueSource entityValueSource = null;

        HasValue<E> field = builder.getField();
        if (field instanceof HasValueSource) {
            ValueSource valueSource = ((HasValueSource) field).getValueSource();
            if (valueSource instanceof EntityValueSource) {
                entityValueSource = (EntityValueSource) valueSource;
                oneToOneComposition = isCompositionProperty(entityValueSource);
            }
        }

        if (builder.getMode() == EditMode.CREATE || (oneToOneComposition && field.getValue() == null)) {
            if (builder.getNewEntity() == null) {
                entity = metadata.create(builder.getEntityClass());
            } else {
                entity = builder.getNewEntity();
            }
            if (container instanceof Nested) {
                initializeNestedEntity(entity, (Nested) container);
            }
            if (oneToOneComposition) {
                Entity ownerEntity = entityValueSource.getItem();
                MetaProperty inverseProp = entityValueSource.getMetaPropertyPath().getMetaProperty().getInverse();
                if (inverseProp != null) {
                    EntityValues.setValue(entity, inverseProp.getName(), ownerEntity);
                }
            }
            if (builder.getInitializer() != null) {
                builder.getInitializer().accept(entity);
            }
        } else {
            entity = builder.getEditedEntity();
        }

        return entity;
    }

    protected boolean isCompositionProperty(EntityValueSource entityValueSource) {
        MetaPropertyPath metaPropertyPath = entityValueSource.getMetaPropertyPath();
        return metaPropertyPath != null
                && metaPropertyPath.getMetaProperty().getType() == MetaProperty.Type.COMPOSITION;
    }

    protected <E extends Entity> Screen createScreen(EditorBuilder<E> builder, Screens screens, E entity) {
        Screen screen;

        if (builder instanceof EditorClassBuilder) {
            @SuppressWarnings("unchecked")
            Class<? extends Screen> screenClass = ((EditorClassBuilder) builder).getScreenClass();

            if (screenClass == null) {
                throw new IllegalArgumentException("Screen class is not set");
            }

            screen = screens.create(screenClass, builder.getLaunchMode(), builder.getOptions());
        } else {
            String editorScreenId;

            if (builder.getScreenId() != null) {
                editorScreenId = builder.getScreenId();
            } else {
                editorScreenId = windowConfig.getEditorScreen(entity).getId();
            }

            if (editorScreenId == null) {
                throw new IllegalArgumentException("Screen id is not set");
            }

            // TODO VM legacy
            // legacy screens support
            WindowInfo windowInfo = windowConfig.getWindowInfo(editorScreenId);
            ScreenOptions options = builder.getOptions();


//
//            if (LegacyFrame.class.isAssignableFrom(windowInfo.getControllerClass())
//                    && options == FrameOwner.NO_OPTIONS) {
//                HashMap<String, Object> paramsMap = new HashMap<>();
//                paramsMap.put(WindowParams.ITEM.name(), entity);
//                options = new MapScreenOptions(paramsMap);
//            }

            screen = screens.create(editorScreenId, builder.getLaunchMode(), options);
        }

        if (!(screen instanceof EditorScreen)) {
            throw new IllegalArgumentException(String.format("Screen %s does not implement EditorScreen: %s",
                    screen.getId(), screen.getClass()));
        }

        return screen;
    }

    protected <E extends Entity> void initializeNestedEntity(E entity, Nested container) {
        InstanceContainer masterContainer = container.getMaster();
        String property = container.getProperty();

        MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
        MetaProperty metaProperty = masterMetaClass.getProperty(property);

        MetaProperty inverseProp = metaProperty.getInverse();
        if (inverseProp != null && !inverseProp.getRange().getCardinality().isMany()) {
            Class<?> inversePropClass = extendedEntities.getEffectiveClass(inverseProp.getDomain());
            Class<?> containerEntityClass = extendedEntities.getEffectiveClass(((CollectionContainer) container).getEntityMetaClass());
            if (inversePropClass.isAssignableFrom(containerEntityClass)) {
                EntityValues.setValue(entity, inverseProp.getName(), masterContainer.getItem());
            }
        }
    }

    @Nullable
    protected DataContext setupParentDataContext(FrameOwner origin, Screen screen, InstanceContainer container,
                                                 @Nullable DataContext parentContext) {
        DataContext dataContext = parentContext;
        if (dataContext == null && container instanceof Nested) {
            InstanceContainer masterContainer = ((Nested) container).getMaster();
            String property = ((Nested) container).getProperty();

            MetaClass masterMetaClass = masterContainer.getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(property);

            if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                dataContext = UiControllerUtils.getScreenData(origin).getDataContext();
            }
        }
        if (dataContext != null) {
            DataContext childContext = UiControllerUtils.getScreenData(screen).getDataContext();
            if (childContext == null) {
                throw new DevelopmentException(
                        String.format("No DataContext found in editor: '%s'", screen.getId()));
            }

            childContext.setParent(dataContext);
        }
        return dataContext;
    }

    protected boolean isCommitCloseAction(CloseAction closeAction) {
        return (closeAction instanceof StandardCloseAction)
                && ((StandardCloseAction) closeAction).getActionId().equals(Window.COMMIT_ACTION_ID);
    }
}
