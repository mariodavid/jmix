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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.xml.layout.loader.TextFieldLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public class CubaTextFieldLoader extends TextFieldLoader {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(io.jmix.ui.component.TextField component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    @Override
    protected Function<?, String> loadFormatter(Element element) {
        Element formatterElement = element.element("formatter");
        if (formatterElement != null) {
            String className = formatterElement.attributeValue("class");

            if (StringUtils.isEmpty(className)) {
                throw new GuiDevelopmentException("Formatter's attribute 'class' is not specified", context);
            }

            Class<?> aClass = getHotDeployManager().findClass(className);
            if (aClass == null) {
                throw new GuiDevelopmentException(String.format("Class %s is not found", className), context);
            }

            try {
                Constructor<?> constructor = aClass.getConstructor(Element.class);
                try {
                    //noinspection unchecked
                    return (Function<?, String>) constructor.newInstance(formatterElement);
                } catch (Throwable e) {
                    throw new GuiDevelopmentException(
                            String.format("Unable to instantiate class %s: %s", className, e.toString()), context);
                }
            } catch (NoSuchMethodException e) {
                try {
                    //noinspection unchecked
                    return (Function<?, String>) aClass.getDeclaredConstructor().newInstance();
                } catch (Exception e1) {
                    throw new GuiDevelopmentException(
                            String.format("Unable to instantiate class %s: %s", className, e1.toString()), context);
                }
            }
        } else {
            return null;
        }
    }
}
