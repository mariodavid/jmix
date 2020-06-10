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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.ui.component.WindowContext;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.compatibility.CubaLegacySettings;
import io.jmix.ui.screen.compatibility.CubaScreensDelegate;
import io.jmix.ui.settings.compatibility.Settings;
import com.haulmont.cuba.settings.SettingsImpl;
import org.springframework.stereotype.Component;

@Component(CubaScreensDelegate.NAME)
public class CubaScreensDelegateImpl implements CubaScreensDelegate {

    @Override
    public void afterShowWindow(Screen screen) {
        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applySettings(getSettingsImpl(screen.getId()));
        }

        if (screen instanceof LegacyFrame) {
            WindowContext windowContext = screen.getWindow().getContext();
            if (!WindowParams.DISABLE_RESUME_SUSPENDED.getBool(windowContext)) {
                DsContext dsContext = ((LegacyFrame) screen).getDsContext();
                if (dsContext != null) {
                    ((DsContextImplementation) dsContext).resumeSuspended();
                }
            }
        }
    }

    @Override
    public void beforeShowWindow(Screen screen) {
        if (screen instanceof CubaLegacySettings) {
            ((CubaLegacySettings) screen).applyDataLoadingSettings(getSettingsImpl(screen.getId()));
        }
    }

    protected Settings getSettingsImpl(String id) {
        return new SettingsImpl(id);
    }
}
