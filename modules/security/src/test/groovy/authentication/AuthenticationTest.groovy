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

package authentication

import io.jmix.core.DataManager
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import io.jmix.data.PersistenceTools
import io.jmix.security.authentication.SecuredAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import test_support.SecuritySpecification

class AuthenticationTest extends SecuritySpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    PersistenceTools persistenceTools

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    InMemoryUserRepository userRepository

    CoreUser user1

    def setup() {
        user1 = new CoreUser("user1", "{noop}123", "user1")
        userRepository.createUser(user1)
    }

    def cleanup() {
        userRepository.removeUser(user1)
    }

    def "standard implementations are in use"() {
        expect:

        userRepository instanceof InMemoryUserRepository
        userRepository.loadUserByUsername('user1') == user1
    }

    def "authenticate with UsernamePasswordAuthenticationToken"() {

        when:

        def token = new UsernamePasswordAuthenticationToken('user1', '123')
        Authentication authentication = authenticationManager.authenticate(token)

        then:

        authentication instanceof SecuredAuthentication
        SecuredAuthentication securedAuthentication = authentication as SecuredAuthentication
        securedAuthentication.user instanceof CoreUser
        securedAuthentication.user == user1

    }

    def "authenticate with SystemAuthenticationToken"() {

        when:

        def token = new SystemAuthenticationToken('user1')
        Authentication authentication = authenticationManager.authenticate(token)

        then:

        authentication instanceof SystemAuthenticationToken
        authentication.isAuthenticated()
        authentication.principal == user1
    }
}
