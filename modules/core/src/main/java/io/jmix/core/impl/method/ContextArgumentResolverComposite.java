/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core.impl.method;

import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import java.util.List;

/**
 * Resolves method parameters by delegating to a list of registered in Spring context
 * {@link MethodArgumentResolver MethodArgumentResolvers}.
 * Previously resolved method parameters are cached for faster lookups.
 */
@Component(ContextArgumentResolverComposite.NAME)
public class ContextArgumentResolverComposite extends CachedArgumentResolverComposite {

    public static final String NAME = "core_ContextArgumentResolverComposite";

    @Autowired
    protected List<MethodArgumentResolver> resolvers;

    /**
     * Return a read-only list with the contained resolvers, or an empty list.
     */
    public List<MethodArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(resolvers);
    }

}
