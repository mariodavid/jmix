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

package io.jmix.core.security.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryUserRepository implements UserRepository {

    protected CoreUser systemUser;
    protected CoreUser anonymousUser;
    protected List<BaseUser> users = new ArrayList<>();

    public InMemoryUserRepository(CoreUser systemUser, CoreUser anonymousUser) {
        this.systemUser = systemUser;
        this.anonymousUser = anonymousUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findAny()
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));
    }

    @Override
    public BaseUser getSystemUser() {
        return systemUser;
    }

    @Override
    public BaseUser getAnonymousUser() {
        return anonymousUser;
    }

    @Override
    public List<BaseUser> getByUsernameLike(String username) {
        return users.stream()
                .filter(user -> user.getUsername().contains(username))
                .collect(Collectors.toList());
    }

    public void createUser(BaseUser user) {
        users.add(user);
    }

    public void removeUser(BaseUser user) {
        users.remove(user);
    }
}
