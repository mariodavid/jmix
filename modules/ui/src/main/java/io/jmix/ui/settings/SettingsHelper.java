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

package io.jmix.ui.settings;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.Window;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;

import java.util.function.Consumer;

public final class SettingsHelper {

    private SettingsHelper() {
    }

    public static <T extends ComponentSettings> T createSettings(Class<T> settingsClass) {
        try {
            return ReflectionHelper.newInstance(settingsClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Cannot create settings '%s'", settingsClass), e);
        }
    }

    public static void lazyTabApplySettings(Window window, Component source, ComponentContainer tabContent) {
        Preconditions.checkNotNullArgument(window);
        Preconditions.checkNotNullArgument(tabContent);

        window.getFacets().forEach(facet -> {
            if (facet instanceof ScreenSettingsFacet) {
                ScreenSettingsFacet settingsFacet = (ScreenSettingsFacet) facet;
                Consumer<ScreenSettingsFacet.SettingsContext> applyHandler = settingsFacet.getApplySettingsDelegate();

                ScreenSettings settings = settingsFacet.getSettings();
                if (settings == null) {
                    throw new IllegalStateException("ScreenSettingsFacet is not attached to the frame");
                }

                if (applyHandler != null) {
                    applyHandler.accept(new ScreenSettingsFacet.SettingsContext(
                            source,
                            tabContent.getComponents(),
                            settings));
                } else {
                    settingsFacet.applySettings(settings);
                }
            }
        });
    }
}
