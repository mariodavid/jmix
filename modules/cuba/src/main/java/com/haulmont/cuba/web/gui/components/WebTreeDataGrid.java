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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.TreeDataGrid;
import com.haulmont.cuba.settings.CubaTreeDataGridSettingsBinder;
import com.haulmont.cuba.settings.component.LegacySettingsApplier;
import com.haulmont.cuba.settings.converter.LegacyTreeDataGridSettingsConverter;
import io.jmix.core.Entity;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;

@Deprecated
public class WebTreeDataGrid<E extends Entity> extends io.jmix.ui.component.impl.WebTreeDataGrid<E>
        implements TreeDataGrid<E> {

    protected LegacySettingsApplier settingsApplier;

    public WebTreeDataGrid() {
        super();

        settingsApplier = createSettingsApplier();
    }

    @Override
    public void applyDataLoadingSettings(Element element) {
        settingsApplier.applyDataLoadingSettings(element);
    }

    @Override
    public void applySettings(Element element) {
        settingsApplier.applySettings(element);
    }

    @Override
    public boolean saveSettings(Element element) {
        return settingsApplier.saveSettings(element);
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsApplier.isSettingsEnabled();
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        settingsApplier.setSettingsEnabled(settingsEnabled);
    }

    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(CubaTreeDataGridSettingsBinder.NAME);
    }

    protected LegacySettingsApplier createSettingsApplier() {
        return new LegacySettingsApplier(this, new LegacyTreeDataGridSettingsConverter(), getSettingsBinder());
    }
}
