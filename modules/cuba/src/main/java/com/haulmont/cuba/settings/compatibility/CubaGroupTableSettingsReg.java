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

package com.haulmont.cuba.settings.compatibility;

import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.web.gui.components.WebGroupTable;
import io.jmix.ui.components.Component;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.GroupTableSettings;
import io.jmix.ui.settings.component.registration.SettingsRegistration;

@org.springframework.stereotype.Component
public class CubaGroupTableSettingsReg implements SettingsRegistration {

    public static final String NAME = "jmix_CubaGroupTableSettingsReg";

    @Override
    public String getComponentName() {
        return GroupTable.NAME;
    }

    @Override
    public Class<? extends Component> getComponentClass() {
        return WebGroupTable.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return GroupTableSettings.class;
    }
}
