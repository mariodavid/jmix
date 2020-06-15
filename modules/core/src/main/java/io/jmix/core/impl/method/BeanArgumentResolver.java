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

package io.jmix.core.impl.method;

import io.jmix.core.BeanLocator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Locale;

/**
 * Allows resolving the current {@link Locale} as method argument.
 * Current user session locale will be returned if user is authorized otherwise will be returned default system locale
 */
@Component(BeanArgumentResolver.NAME)
public class BeanArgumentResolver implements MethodArgumentResolver {

    public static final String NAME = "core_BeanArgumentResolver";

    @Autowired
    protected BeanLocator beanLocator;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        try {
            beanLocator.get(parameter.getParameterType());
            return true;
        } catch (NoSuchBeanDefinitionException ignored) {
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter) {
        try {
            return beanLocator.get(parameter.getParameterType());
        } catch (NoSuchBeanDefinitionException ignored) {
        }
        return null;
    }

}
