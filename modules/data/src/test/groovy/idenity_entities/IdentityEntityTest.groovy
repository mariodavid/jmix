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

package idenity_entities

import io.jmix.core.entity.EntityValues
import test_support.entity.TestIdentityIdEntity
import test_support.entity.TestIdentityUuidEntity
import io.jmix.core.DataManager
import io.jmix.core.entity.IdProxy
import test_support.DataSpec

import org.springframework.beans.factory.annotation.Autowired

class IdentityEntityTest extends DataSpec {

    @Autowired DataManager dataManager

    def "equality: newly created instances are not equal"() {
        when:

        def entity1 = dataManager.create(TestIdentityIdEntity)

        then:

        EntityValues.getId(entity1) != null

        when:

        def entity2 = dataManager.create(TestIdentityIdEntity)

        then:

        entity1 != entity2
    }

    def "equality: committed and loaded instances with the same id are equal"() {

        def entity1 = dataManager.create(TestIdentityIdEntity)

        when:

        def entity11 = dataManager.save(entity1)
        def entity12 = dataManager.load(TestIdentityIdEntity).id(IdProxy.of(entity11.id)).one()

        then:

        entity11 == entity12
        !entity11.is(entity12)
    }

    def "equality of HasUuid: committed and loaded instances with the same id are equal"() {

        def entity1 = dataManager.create(TestIdentityUuidEntity)

        when:

        def entity11 = dataManager.save(entity1)
        def entity12 = dataManager.load(TestIdentityUuidEntity).id(IdProxy.of(entity11.id)).one()

        then:

        entity11 == entity12
        !entity11.is(entity12)
    }

    def "storing in hashtables"() {

        def entity1 = dataManager.create(TestIdentityIdEntity)
        def entity11 = dataManager.save(entity1)
        def idVal = entity11.id
        def uuid = EntityValues.getId(entity11).uuid

        def map = new HashMap()

        when:

        map.put(EntityValues.getId(entity11), entity11)

        then:

        map.get(IdProxy.of(idVal, uuid)).is(entity11)
    }
}
