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

package io.jmix.ui.presentations.model;

import io.jmix.core.Entity;
import io.jmix.ui.presentations.Presentations;

import java.util.UUID;

/**
 * Base interface for Presentation entity.
 * <p>
 * <br>
 * Note that by default, UI does not provide persistence functionality for presentations. To save/load presentations add
 * "ui-persistence" add-on.
 *
 * @see Presentations
 */
public interface Presentation extends Entity<UUID> {

    String getName();

    void setName(String name);

    String getXml();

    void setXml(String xml);

    String getUserLogin();

    void setUserLogin(String userLogin);

    Boolean getDefault();

    void setDefault(Boolean isDefault);

    Boolean getAutoSave();

    void setAutoSave(Boolean autoSave);

    void setComponentId(String componentId);

}
