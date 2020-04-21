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

package io.jmix.core.security;

import io.jmix.core.entity.BaseUser;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class that is used to get the information about the authenticated user.
 */
public interface CurrentAuthentication {

    String NAME = "jmix_CurrentAuthentication";

    @Nullable
    Authentication getAuthentication();

    BaseUser getUser();

    Locale getLocale();

    TimeZone getTimeZone();

    boolean isAuthenticated();
}
