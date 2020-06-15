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

package io.jmix.ui.screen.compatibility;

import io.jmix.ui.Screens;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Screen.AfterShowEvent;
import io.jmix.ui.screen.Screen.BeforeShowEvent;

/**
 * Delegates legacy functionality from {@link Screens}.
 */
public interface CubaScreensDelegate {

    String NAME = "jmix_CubaSettingsLifecycle";

    /**
     * Is invoked before {@link AfterShowEvent}.
     *
     * @param screen screen to show
     */
    void afterShowWindow(Screen screen);

    /**
     * Is invoked before {@link BeforeShowEvent}.
     *
     * @param screen screen to show
     */
    void beforeShowWindow(Screen screen);

    /**
     * Saves settings only for legacy screens.
     *
     * @param screens screens
     * @see CubaLegacySettings
     */
    void saveScreenSettings(Screens screens);
}