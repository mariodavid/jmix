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

package io.jmix.ui.icon;

import com.vaadin.server.Resource;

/**
 * A facade bean that is intended for resolving icon resources by the given {@code iconPath} using
 * all available {@link IconProvider} beans.
 */
public interface IconResolver {
    String NAME = "ui_IconResolver";

    /**
     * Returns an instance of {@link Resource} by the given {@code iconPath}.
     *
     * @param iconPath icon path
     * @return {@link Resource} instance
     */
    Resource getIconResource(String iconPath);
}