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

package com.haulmont.cuba.gui.components.presentation;

import com.haulmont.cuba.gui.components.HasSettings;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.presentation.action.SavePresentationAction;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.screen.compatibility.CubaLegacySettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.dom4j.Element;

public class CubaSavePresentationAction extends SavePresentationAction {

    public CubaSavePresentationAction(Table table, ComponentSettingsBinder settingsBinder) {
        super(table, settingsBinder);
    }

    @Override
    protected void setSettingsToPresentation(TablePresentations presentations, TablePresentation current) {
        if (table.getFrame().getFrameOwner() instanceof CubaLegacySettings) {
            Element e = presentations.getSettings(current);
            ((HasSettings) table).saveSettings(e);
            presentations.setSettings(current, e);
        } else {
            super.setSettingsToPresentation(presentations, current);
        }
    }
}
