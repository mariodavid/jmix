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

package annotated_role_builder


import io.jmix.security.model.Role
import io.jmix.security.role.builder.AnnotatedRoleBuilder
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityUiSpecification
import test_support.annotated_role_builder.TestMultipleScreenPoliciesOnMethodRole
import test_support.annotated_role_builder.TestScreen1
import test_support.annotated_role_builder.TestScreen2

class UiAnnotatedRoleBuilderTest extends SecurityUiSpecification {

    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder

    def "multiple @ScreenPolicy on single method"() {
        when:
        Role role = annotatedRoleBuilder.createRole(TestMultipleScreenPoliciesOnMethodRole.class.getCanonicalName())

        then:

        role.code == 'testMyRole1'
        role.name == 'Test My Role 1'
        def resourcePolicies = role.resourcePolicies
        resourcePolicies.size() == 4

        resourcePolicies.find {it.resource == TestScreen1.ID} != null
        resourcePolicies.find {it.resource == TestScreen2.ID} != null
        resourcePolicies.find {it.resource == 'screen1'} != null
        resourcePolicies.find {it.resource == 'screen2'} != null
    }

}
