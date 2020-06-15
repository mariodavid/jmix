/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.core.Events;
import io.jmix.core.Entity;
import io.jmix.data.event.EntityPersistingEvent;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component(EntityPersistingEventManager.NAME)
public class EntityPersistingEventManager {

    public static final String NAME = "data_EntityPersistingEventManager";

    @Autowired
    protected Events events;

    public void publishEvent(Entity entity) {
        EntityPersistingEvent<Entity> event = new EntityPersistingEvent<>(this, entity);
        events.publish(event);
    }
}
