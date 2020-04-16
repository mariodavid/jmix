/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.settings.component;

import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.component.registration.SettingsRegistration;

import java.util.UUID;

/**
 * Base interface for component settings. See {@link TableSettings} as an example.
 *
 * @see ScreenSettings
 * @see SettingsRegistration
 */
public interface ComponentSettings {

    String getId();

    void setId(String id);

    interface HasSettingsPresentation {

        UUID getPresentationId();
    }
}
