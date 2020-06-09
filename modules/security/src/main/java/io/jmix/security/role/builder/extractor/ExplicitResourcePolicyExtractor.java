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

package io.jmix.security.role.builder.extractor;

import io.jmix.security.model.ResourcePolicy;
import io.jmix.security.role.annotation.ExplicitResourcePolicies;
import org.springframework.stereotype.Component;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.Collections;

@Component(ExplicitResourcePolicyExtractor.NAME)
public class ExplicitResourcePolicyExtractor implements ResourcePolicyExtractor {

    public static final String NAME = "sec_ExplicitResourcePolicyExtractor";

    @Override
    public Collection<ResourcePolicy> extractResourcePolicies(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            ExplicitResourcePolicies annotation = method.getAnnotation(ExplicitResourcePolicies.class);
            if (annotation != null) {
                if (isMethodReturnCollectionOfResourcePolicies(method)) {
                    try {
                        return (Collection<ResourcePolicy>) method.invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Cannot evaluate resource policies", e);
                    }
                } else {
                    String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
                    throw new RuntimeException("Method " + methodName + " with ExplicitResourcePolicies annotation should return a " +
                            "collection of ResourcePolicy");
                }
            }
        }
        return Collections.emptyList();
    }

    private boolean isMethodReturnCollectionOfResourcePolicies(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!Collection.class.isAssignableFrom(returnType)) return false;

        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if (typeArguments.length != 1) return false;
            Class argClass = (Class) typeArguments[0];
            if (ResourcePolicy.class.isAssignableFrom(argClass)) return true;
        }
        return false;
    }
}
