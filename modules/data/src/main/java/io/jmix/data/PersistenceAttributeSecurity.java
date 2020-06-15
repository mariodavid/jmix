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

package io.jmix.data;

import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;

import java.util.Collection;

/**
 * Enforces entity attribute permissions on the data access layer.
 */
public interface PersistenceAttributeSecurity {

    String NAME = "data_PersistenceAttributeSecurity";

    FetchPlan createRestrictedFetchPlan(FetchPlan fetchPlan);

    void afterLoad(Entity entity);

    void afterLoad(Collection<? extends Entity> entities);

    void beforePersist(Entity entity);

    void beforeMerge(Entity entity);

    void afterCommit(Entity entity);
}
