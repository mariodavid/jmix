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

package security

import io.jmix.core.CoreConfiguration
import io.jmix.core.entity.BaseUser
import io.jmix.core.security.SystemAuthenticationToken
import io.jmix.core.security.impl.AuthenticatorImpl
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import test_support.AppContextTestExecutionListener

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [CoreConfiguration])
@TestPropertySource(properties = ["jmix.securityImplementation = core"])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class AuthenticatorTest extends Specification {

    @Autowired
    AuthenticatorImpl authenticator

    @Autowired
    InMemoryUserRepository userRepository

    CoreUser admin

    def setup() {
        admin = new CoreUser('admin', '{noop}admin123', 'Admin')
        userRepository.createUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }


    def "authenticate as system"() {
        when:

        authenticator.begin()

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof BaseUser
        ((BaseUser) authentication.principal).username == 'system'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "authenticate as admin"() {
        when:

        authenticator.begin('admin')

        then:

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        authentication instanceof SystemAuthenticationToken
        authentication.principal instanceof BaseUser
        ((BaseUser) authentication.principal).username == 'admin'

        when:

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }

    def "nested authentication"() {

        when: "outer auth"

        authenticator.begin()

        then:

        Authentication outerAuth = SecurityContextHolder.getContext().getAuthentication()
        outerAuth instanceof SystemAuthenticationToken
        outerAuth.principal instanceof BaseUser
        ((BaseUser) outerAuth.principal).username == 'system'

        when: "inner auth"

        authenticator.begin('admin')

        then:

        Authentication innerAuth = SecurityContextHolder.getContext().getAuthentication()
        innerAuth instanceof SystemAuthenticationToken
        innerAuth.principal instanceof BaseUser
        ((BaseUser) innerAuth.principal).username == 'admin'

        when: "end inner"

        authenticator.end()

        then:

        Authentication outerAuth1 = SecurityContextHolder.getContext().getAuthentication()
        outerAuth1 instanceof SystemAuthenticationToken
        outerAuth1.principal instanceof BaseUser
        ((BaseUser) outerAuth1.principal).username == 'system'

        when: "end outer"

        authenticator.end()

        then:

        SecurityContextHolder.getContext().getAuthentication() == null
    }
}
