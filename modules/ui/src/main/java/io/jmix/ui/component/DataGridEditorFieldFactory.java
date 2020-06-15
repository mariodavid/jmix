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

package io.jmix.ui.component;

import io.jmix.ui.component.data.meta.EntityValueSource;

/**
 * Factory that generates components for {@link DataGrid} editor.
 */
public interface DataGridEditorFieldFactory {
    String NAME = "ui_DataGridEditorFieldFactory";

    /**
     * Generates component for {@link DataGrid} editor.
     *
     * @param datasource editing item datasource
     * @param property   editing item property
     * @return generated component
     * @throws IllegalStateException if created component doesn't implement the {@link Field} interface
     * @deprecated Use {@link #createField(EntityValueSource, String)} instead
     */
    /*
    TODO: legacy-ui
    @Deprecated
    Field createField(Datasource datasource, String property);*/

    /**
     * Generates component for {@link DataGrid} editor.
     *
     * @param valueSource editing item value source
     * @param property    editing item property
     * @return generated component
     * @throws IllegalStateException if created component doesn't implement the {@link Field} interface
     */
    Field createField(EntityValueSource valueSource, String property);
}
