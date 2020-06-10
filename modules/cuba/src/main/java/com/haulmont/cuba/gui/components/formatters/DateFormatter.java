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

package com.haulmont.cuba.gui.components.formatters;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import org.dom4j.Element;

import java.util.Date;

/**
 * @deprecated Use {@link io.jmix.ui.component.formatter.DateFormatter} instead
 */
@Deprecated
public class DateFormatter extends io.jmix.ui.component.formatter.DateFormatter {

    public DateFormatter(Element element) {
        super(element);
    }

    @Override
    public String apply(Date value) {
        messages = AppBeans.get(Messages.class);
        currentAuthentication = AppBeans.get(CurrentAuthentication.class);
        return super.apply(value);
    }
}
