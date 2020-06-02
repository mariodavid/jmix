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

import com.haulmont.cuba.gui.components.TreeTable;
import com.haulmont.cuba.gui.components.presentation.CubaPresentationActionsBuilder;
import com.haulmont.cuba.settings.CubaTreeTableSettingsBinder;
import com.haulmont.cuba.settings.component.LegacySettingsApplier;
import io.jmix.core.Entity;
import io.jmix.ui.component.presentation.TablePresentationsLayout;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.compatibility.CubaLegacySettings;
import com.haulmont.cuba.settings.converter.LegacyTableSettingsConverter;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;

@Deprecated
public class WebTreeTable<E extends Entity> extends io.jmix.ui.component.impl.WebTreeTable<E> implements TreeTable<E> {

    protected LegacySettingsApplier settingsApplier;

    public WebTreeTable() {
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

    @Override
    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(CubaTreeTableSettingsBinder.NAME);
    }

    protected LegacySettingsApplier createSettingsApplier() {
        return new LegacySettingsApplier(this, new LegacyTableSettingsConverter(), getSettingsBinder());
    }

    @Override
    protected TablePresentationsLayout createPresentationsLayout() {
        TablePresentationsLayout layout = super.createPresentationsLayout();
        layout.setPresentationActionsBuilder(new CubaPresentationActionsBuilder(this, getSettingsBinder()));
        layout.build();
        return layout;
    }

    @Override
    protected void updatePresentationSettings(TablePresentations p) {
        if (getFrame().getFrameOwner() instanceof CubaLegacySettings) {
            Element e = p.getSettings(p.getCurrent());
            saveSettings(e);
            p.setSettings(p.getCurrent(), e);
        } else {
            super.updatePresentationSettings(p);
        }
    }

    @Override
    protected void applyPresentationSettings(TablePresentation p) {
        if (getFrame().getFrameOwner() instanceof CubaLegacySettings) {
            Element settingsElement = presentations.getSettings(p);
            applySettings(settingsElement);
        } else {
            super.applyPresentationSettings(p);
        }
    }
}
