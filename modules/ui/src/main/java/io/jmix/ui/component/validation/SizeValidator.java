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

package io.jmix.ui.component.validation;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import io.jmix.ui.component.ValidationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.Map;

/**
 * Size validator is applicable for Collections and String values. It checks that value is in a specific range.
 * <p>
 * For error message it uses Groovy string and it is possible to use following keys for formatted output: 'value', 'min' and 'max'.
 * <p>
 * Note, that size validator for Collection doesn't use key 'value' for output error message.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 *     &lt;bean id="ui_SizeValidator" class="io.jmix.ui.component.validation.SizeValidator" scope="prototype"/&gt;
 *     </pre>
 * Use {@link BeanLocator} when creating the validator programmatically.
 *
 * @param <T> Collection or String
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(SizeValidator.NAME)
public class SizeValidator<T> extends AbstractValidator<T> {

    public static final String NAME = "ui_SizeValidator";

    protected int min;
    protected int max = Integer.MAX_VALUE;

    protected String defaultMessage;

    public SizeValidator() {
    }

    /**
     * Constructor for custom error message. This message can contain following keys formatted output:
     * '$value', '$min', and '$max'.
     * <p>
     * Example: "The '$value' length must be between $min and $max".
     * <p>
     * Note, that message for Collection doesn't use '$value' key for output error message.
     *
     * @param message error message
     */
    public SizeValidator(String message) {
        this.message = message;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    /**
     * Sets min value of the range. Min value cannot be less than 0. Default value is 0.
     * <p>
     * Note, min value is included in range. Examples:
     * <pre>{@code
     *  value = 0, min = 0 - is valid
     *  value = 1, min = 2 - is not valid
     * }
     * </pre>
     *
     * @param min min value
     */
    public void setMin(int min) {
        checkPositiveValue(min, "Min value cannot be less then 0");
        checkRange(min, max);

        this.min = min;
    }

    public int getMin() {
        return min;
    }

    /**
     * Sets max value of the range. Max value cannot be less than 0.  Default value is {@link Integer#MAX_VALUE}.
     * <p>
     * Note, max value is included in range. Examples:
     * <pre>{@code
     *  value = 5, max = 5 - is valid
     *  value = 6, max = 5 - is not valid
     * }
     * </pre>
     *
     * @param max max value
     */
    public void setMax(int max) {
        checkPositiveValue(max, "Max value cannot be less then 0");
        checkRange(min, max);

        this.max = max;
    }

    /**
     * @return max value of the range
     */
    public int getMax() {
        return max;
    }

    /**
     * Sets min and max range values. Min and max values cannot be less than 0.
     *
     * @param min min value
     * @param max max value
     */
    public void setSize(int min, int max) {
        checkPositiveValue(min, "Min value cannot be less then 0");
        checkPositiveValue(max, "Max value cannot be less then 0");
        checkRange(min, max);

        this.min = min;
        this.max = max;
    }

    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is in range
        if (value == null) {
            return;
        }

        String message = getMessage();
        Class clazz = value.getClass();
        if (Collection.class.isAssignableFrom(clazz)) {
            int size = ((Collection) value).size();
            if (min > size || size > max) {
                this.defaultMessage = messages.getMessage("validation.constraints.collectionSizeRange");

                fireValidationException(
                        message == null ? defaultMessage : message,
                        ParamsMap.of("min", min, "max", max));
            }
        } else if (clazz.equals(String.class)) {
            int length = ((String) value).length();
            if (min > length || length > max) {
                this.defaultMessage = messages.getMessage("validation.constraints.sizeRange");

                fireValidationException(
                        message == null ? defaultMessage : message,
                        ParamsMap.of("value", value,
                                "min", min,
                                "max", max));
            }
        }
    }

    protected void checkPositiveValue(long value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    protected void checkRange(int min, int max) {
        if (min > max) {
            throw new IllegalStateException("Min value cannot be greater than max");
        }
    }

    protected void fireValidationException(String errorMessage, Map<String, Object> map) {
        throw new ValidationException(getTemplateErrorMessage(errorMessage, map));
    }
}
