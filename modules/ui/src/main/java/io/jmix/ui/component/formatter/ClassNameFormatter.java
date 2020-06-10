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
package io.jmix.ui.component.formatter;

import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component(ClassNameFormatter.NAME)
@Scope("prototype")
public class ClassNameFormatter implements Function<Object, String> {

    public static final String NAME = "jmix_ClassNameFormatter";

    @Autowired
    protected Messages messages;

    @Override
    public String apply(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            String str = (String) value;
            final int i = str.lastIndexOf(".");
            if (i < 0) {
                return str;
            } else {
                return messages.getMessage(str.substring(0, i), str.substring(i + 1, str.length()));
            }
        }
        return null;
    }
}
