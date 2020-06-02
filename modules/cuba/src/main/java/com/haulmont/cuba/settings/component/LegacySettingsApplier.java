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

package com.haulmont.cuba.settings.component;

import com.haulmont.cuba.gui.components.HasDataLoadingSettings;
import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.component.Component;
import com.haulmont.cuba.settings.converter.LegacySettingsConverter;
import io.jmix.ui.settings.component.DataGridSettings;
import io.jmix.ui.settings.component.SettingsWrapperImpl;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import org.dom4j.Element;

@SuppressWarnings({"rawtypes", "unchecked"})
public class LegacySettingsApplier implements HasSettings, HasDataLoadingSettings {

    protected Component component;

    protected LegacySettingsConverter settingsConverter;
    protected ComponentSettingsBinder settingsBinder;

    protected boolean settingsEnabled = true;

    public LegacySettingsApplier(Component component,
                                 LegacySettingsConverter settingsConverter,
                                 ComponentSettingsBinder settingsBinder) {
        this.component = component;
        this.settingsConverter = settingsConverter;
        this.settingsBinder = settingsBinder;
    }

    @Override
    public void applySettings(Element element) {
        if (!isSettingsEnabled()) {
            return;
        }

        DataGridSettings dataGridSettings = settingsConverter.convertToComponentSettings(element);

        settingsBinder.applySettings(component, new SettingsWrapperImpl(dataGridSettings));
    }

    @Override
    public void applyDataLoadingSettings(Element element) {
        if (!isSettingsEnabled()) {
            return;
        }
        DataGridSettings dataGridSettings = settingsConverter.convertToComponentSettings(element);

        ((DataLoadingSettingsBinder) settingsBinder)
                .applyDataLoadingSettings(component, new SettingsWrapperImpl(dataGridSettings));
    }

    @Override
    public boolean saveSettings(Element element) {
        if (!isSettingsEnabled()) {
            return false;
        }

        DataGridSettings dataGridSettings = settingsConverter.convertToComponentSettings(element);

        boolean modified = settingsBinder.saveSettings(component, new SettingsWrapperImpl(dataGridSettings));
        if (modified)
            settingsConverter.copyToElement(dataGridSettings, element);

        return modified;
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsEnabled;
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        this.settingsEnabled = settingsEnabled;
    }
}
