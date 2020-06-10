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

import io.jmix.core.MetadataTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(CollectionFormatter.NAME)
@Scope("prototype")
public class CollectionFormatter implements Function<Collection, String> {

    public static final String NAME = "jmix_CollectionFormatter";

    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public String apply(Collection value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        //noinspection unchecked
        return ((Collection<Object>) value).stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "));
    }
}
