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

package com.haulmont.cuba.core.global.impl;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Entity;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;

import java.sql.Connection;

public class EntityListenerUtils {

    public static EntityManager getCurrentEntityManager(Entity entity) {
        Metadata metadata = AppBeans.get(Metadata.class);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        String storeName = metadataTools.getStoreName(metadata.getClass(entity));
        Persistence persistence = AppBeans.get(Persistence.class);
        return persistence.getEntityManager(storeName);
    }

    public static Connection getCurrentConnection(Entity entity) {
        return getCurrentEntityManager(entity).getConnection();
    }
}
