/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.data.impl;

import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.IdSequence;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.DataProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Intermediate cache for generated ids of entities with long/integer PK.
 * The cache size is determined by the {@code cuba.numberIdCacheSize} app property.
 */
@Component(NumberIdCache.NAME)
public class NumberIdCache {

    public static final String NAME = "data_NumberIdCache";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected NumberIdWorker numberIdWorker;
    @Autowired
    protected DataProperties dataProperties;

    protected ConcurrentMap<String, Generator> cache = new ConcurrentHashMap<>();

    protected class Generator {
        protected long counter;
        protected long sequenceValue;
        protected String entityName;
        protected String sequenceName;
        protected boolean cached;

        public Generator(String entityName,
                         @Nullable String sequenceName,
                         boolean cached) {
            this.entityName = entityName;
            this.sequenceName = sequenceName;
            this.cached = cached;
            if (useIdCache()) {
                createCachedCounter();
            }
        }

        protected boolean useIdCache() {
            return dataProperties.getNumberIdCacheSize() != 0 && cached;
        }

        protected void createCachedCounter() {
            sequenceValue = numberIdWorker.createCachedLongId(entityName, sequenceName);
            counter = sequenceValue;
        }

        public synchronized long getNext() {
            if (!useIdCache()) {
                return numberIdWorker.createLongId(entityName, sequenceName);
            } else {
                long next = ++counter;
                if (next > sequenceValue + dataProperties.getNumberIdCacheSize()) {
                    createCachedCounter();
                    next = ++counter;
                }
                return next;
            }
        }
    }

    /**
     * Generates next id.
     *
     * @param entityName entity name
     * @return next id
     */
    public Long createLongId(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        final boolean cached;
        final String sequenceName;
        if (metaClass != null) {
            Map attributes = (Map) metaClass.getAnnotations().get(IdSequence.class.getName());
            if (attributes != null) {
                sequenceName = (String) attributes.get("name");
                cached = Boolean.TRUE.equals(attributes.get("cached"));
            } else {
                cached = true;
                sequenceName = null;
            }
        } else {
            cached = true;
            sequenceName = null;
        }

        Generator gen = cache.computeIfAbsent(getCacheKey(entityName, sequenceName), s -> new Generator(entityName, sequenceName, cached));
        return gen.getNext();
    }

    /**
     * INTERNAL. Used by tests.
     */
    public void reset() {
        cache.clear();
    }

    protected String getCacheKey(String entityName, @Nullable String sequenceName) {
        return sequenceName == null ? entityName : sequenceName;
    }
}
