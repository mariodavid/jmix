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

package com.haulmont.cuba.gui.components;

import io.jmix.ui.settings.component.binder.DataLoadingSettingsBinder;
import io.jmix.ui.persistence.settings.facet.ScreenSettingsFacet;
import org.dom4j.Element;

/**
 * @deprecated to use component settings add "ui-persistence" add-on. See {@link DataLoadingSettingsBinder} and
 * {@link ScreenSettingsFacet}.
 */
@Deprecated
public interface HasDataLoadingSettings {

    /**
     * @deprecated for component settings use "ui-persistence" add-on and {@link DataLoadingSettingsBinder} instead.
     */
    @Deprecated
    void applyDataLoadingSettings(Element element);
}
