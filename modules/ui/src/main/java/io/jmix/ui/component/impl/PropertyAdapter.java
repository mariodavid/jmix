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
package io.jmix.ui.component.impl;

import com.vaadin.v7.data.Property;

public abstract class PropertyAdapter implements Property, Property.ValueChangeNotifier {
    protected final Property itemProperty;

    public PropertyAdapter(Property itemProperty) {
        if (itemProperty == null) throw new IllegalStateException("Property is null");
        this.itemProperty = itemProperty;
    }

    @Override
    public Class getType() {
        return itemProperty.getType();
    }

    @Override
    public boolean isReadOnly() {
        return itemProperty.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean newStatus) {
        itemProperty.setReadOnly(newStatus);
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        addValueChangeListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        removeValueChangeListener(listener);
    }

    @Override
    public void addValueChangeListener(ValueChangeListener listener) {
        if (itemProperty instanceof Property.ValueChangeNotifier) {
            ((ValueChangeNotifier) itemProperty).addValueChangeListener(listener);
        }
    }

    @Override
    public void removeValueChangeListener(ValueChangeListener listener) {
        if (itemProperty instanceof Property.ValueChangeNotifier) {
            ((ValueChangeNotifier) itemProperty).removeValueChangeListener(listener);
        }
    }
}