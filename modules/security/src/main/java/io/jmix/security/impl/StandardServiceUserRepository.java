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

package io.jmix.security.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.ServiceUserRepository;
import io.jmix.security.OnStandardSecurityImplementation;
import io.jmix.security.entity.User;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component(ServiceUserRepository.NAME)
@Conditional(OnStandardSecurityImplementation.class)
public class StandardServiceUserRepository implements ServiceUserRepository {

    private User systemUser;
    private User anonymousUser;

    public StandardServiceUserRepository() {
        systemUser = createSystemUser();
        anonymousUser = createAnonymousUser();
    }

    private User createSystemUser() {
        User user = new User();
        user.setUsername("system");
        user.setName("system");
        return user;
    }

    private User createAnonymousUser() {
        User user = new User();
        user.setUsername("anonymous");
        user.setName("anonymous");
        return user;
    }

    @Override
    public BaseUser getSystemUser() {
        return systemUser;
    }

    @Override
    public BaseUser getAnonymousUser() {
        return anonymousUser;
    }
}
