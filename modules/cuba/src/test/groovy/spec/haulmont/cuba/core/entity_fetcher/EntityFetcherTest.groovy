/*
 * Copyright (c) 2008-2020 Haulmont.
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

package spec.haulmont.cuba.core.entity_fetcher

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.model.Many2ManyA
import com.haulmont.cuba.core.model.Many2ManyB
import com.haulmont.cuba.core.model.Many2ManyRef
import com.haulmont.cuba.core.testsupport.TestSupport
import io.jmix.core.DataManager
import io.jmix.core.FetchPlanBuilder
import io.jmix.core.Id
import io.jmix.data.EntityFetcher
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class EntityFetcherTest extends CoreTestSpecification {

    @Autowired
    private DataManager dataManager
    @Autowired
    private EntityFetcher entityFetcher
    @Autowired
    private Persistence persistence
    @Autowired
    private TestSupport testSupport

    def "fetching entity with many-to-many collection containing detached instances"() {
        def ref = new Many2ManyRef(name: 'ref1')
        def b1 = new Many2ManyB()
        def a1 = new Many2ManyA(ref: ref, collectionOfB: [b1])
        dataManager.save(ref, b1, a1)

        def loadedB1 = dataManager.load(Id.of(b1)).fetchPlan { vb -> vb.addAll('collectionOfA')}.one()

        when:
        def a2 = new Many2ManyA(collectionOfB: [loadedB1])

        persistence.callInTransaction { em ->
            em.persist(a2)

            entityFetcher.fetch(a2, FetchPlanBuilder.of(Many2ManyA).addAll('collectionOfB.collectionOfA.ref').build())
            return a2
        }

        then:
        noExceptionThrown()

        cleanup:
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource())
        jdbcTemplate.update('delete from TEST_MANY2MANY_AB_LINK where A_ID = ?', a1.id)
        jdbcTemplate.update('delete from TEST_MANY2MANY_AB_LINK where A_ID = ?', a2.id)

        testSupport.deleteRecord(a1, a2, b1, ref)
    }
}
