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

package com.haulmont.cuba.core.testsupport;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.core.security.impl.CoreUser;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class TestCurrentAuthentication implements CurrentAuthentication {

    public static final String USER_ID = "60885987-1b61-4247-94c7-dff348347f93";
    protected CoreUser user;

    public TestCurrentAuthentication() {
        user = new CoreUser("test_admin", "test_admin", "Test Administrator");
        user.setId(UUID.fromString(USER_ID));
    }

    @Nullable
    @Override
    public Authentication getAuthentication() {
        return new SystemAuthenticationToken(getUser(), new ArrayList<>());
    }

    @Override
    public BaseUser getUser() {
        return user;
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public boolean isSet() {
        return true;
    }
}
