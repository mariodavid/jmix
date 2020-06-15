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

package metadata

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.Stores
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import spock.lang.Specification
import test_support.app.entity.Address
import test_support.app.entity.Owner
import test_support.base.entity.BaseGenericIdEntity
import test_support.base.entity.BaseUuidEntity
import test_support.base.entity.StandardEntity
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.AppContextTestExecutionListener
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet

import org.springframework.beans.factory.annotation.Autowired

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
@TestExecutionListeners(value = AppContextTestExecutionListener,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class MetadataTest extends Specification {

    @Autowired
    Metadata metadata

    def "entities are in metadata"() {
        expect:

        metadata.findClass(StandardEntity) != null
        metadata.findClass(TestAddon1Entity) != null
    }

    def "ancestors and descendants are collected recursively"() {

        def pet = metadata.getClass(Pet)
        def standardEntity = metadata.getClass(StandardEntity)
        def baseUuidEntity = metadata.getClass(BaseUuidEntity)
        def baseGenericIdEntity = metadata.getClass(BaseGenericIdEntity)

        expect:

        pet.ancestor == standardEntity
        pet.ancestors[0] == pet.ancestor
        pet.ancestors[1] == baseUuidEntity
        pet.ancestors[2] == baseGenericIdEntity

        baseGenericIdEntity.descendants.containsAll([baseUuidEntity, standardEntity, pet])
        baseUuidEntity.descendants.containsAll([standardEntity, pet])
        standardEntity.descendants.containsAll([pet])
    }

    def "inherited properties"() {

        def baseMetaClass = metadata.getClass(StandardEntity)
        def baseProp = baseMetaClass.getProperty('createTs')
        def baseIdProp = baseMetaClass.getProperty('id')

        def entityMetaClass = metadata.getClass(TestAddon1Entity)
        def entityProp = entityMetaClass.getProperty('createTs')
        def entityIdProp = entityMetaClass.getProperty('id')

        expect:

        !entityProp.is(baseProp)
        baseProp.domain == baseMetaClass
        entityProp.domain == entityMetaClass

        entityProp.range == baseProp.range
        entityProp.annotatedElement == baseProp.annotatedElement
        entityProp.declaringClass == baseProp.declaringClass
        entityProp.inverse == baseProp.inverse
        entityProp.javaType == baseProp.javaType
        entityProp.mandatory == baseProp.mandatory
        entityProp.readOnly == baseProp.readOnly

        !entityIdProp.is(baseIdProp)
        entityIdProp.domain == entityMetaClass
        entityIdProp.range == baseIdProp.range
        entityIdProp.annotatedElement == baseIdProp.annotatedElement
    }

    def "store of entity is NOOP"() {

        def metaClass = metadata.getClass(TestAddon1Entity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.NOOP
    }

    def "store of mapped superclass is UNDEFINED"() {

        def metaClass = metadata.getClass(StandardEntity)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.UNDEFINED
    }

    def "store of embeddable and its properties is MAIN"() {

        def metaClass = metadata.getClass(Address)

        expect:

        metaClass.store != null
        metaClass.store.name == Stores.MAIN
        metaClass.getProperty('city').store.name == Stores.MAIN
    }

    def "store of embedded property is MAIN"() {

        def metaClass = metadata.getClass(Owner)
        def property = metaClass.getProperty('address')

        expect:

        property.store.name == Stores.MAIN
    }

    def "store of entity property is NOOP"() {

        def metaProperty = metadata.getClass(TestAddon1Entity).getProperty('name')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.NOOP
    }

    def "store of mapped superclass property is UNDEFINED"() {

        def metaProperty = metadata.getClass(StandardEntity).getProperty('createTs')

        expect:

        metaProperty.store != null
        metaProperty.store.name == Stores.UNDEFINED
    }

    def "store of entity property inherited from mapped superclass is NOOP"() {

        def idProp = metadata.getClass(TestAddon1Entity).getProperty('id')
        def createTsProp = metadata.getClass(TestAddon1Entity).getProperty('createTs')

        expect:

        idProp.store.name == Stores.NOOP
        createTsProp.store.name == Stores.NOOP
    }

    def "store of entity annotated with @Entity is MAIN"() {

        def metaClass = metadata.getClass(Pet)
        def idProp = metaClass.getProperty('id')
        def nameProp = metaClass.getProperty('name')

        expect:

        idProp.store.name == Stores.MAIN
        nameProp.store.name == Stores.MAIN
    }

    def "store of non-mapped property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClass(Pet)
        def nickProp = metaClass.getProperty('nick')

        expect:

        nickProp.store.name == Stores.UNDEFINED
    }

    def "store of method-based property of entity annotated with @Entity is UNDEFINED"() {

        def metaClass = metadata.getClass(Pet)
        def descriptionProp = metaClass.getProperty('description')

        expect:

        descriptionProp.store.name == Stores.UNDEFINED
    }
}
