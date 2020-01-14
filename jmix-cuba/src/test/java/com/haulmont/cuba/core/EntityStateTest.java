/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CubaCoreTest;
import com.haulmont.cuba.core.testsupport.TestContainer;
import io.jmix.core.entity.BaseEntityInternalAccess;
import io.jmix.data.EntityManager;
import io.jmix.data.Query;
import io.jmix.data.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.haulmont.cuba.core.testsupport.TestSupport.reserialize;
import static org.junit.jupiter.api.Assertions.*;

@CubaCoreTest
public class EntityStateTest {

    public static TestContainer cont = TestContainer.Common.INSTANCE;

    private UUID userId;
    private Group group;

    @AfterEach
    public void tearDown() throws Exception {
        if (userId != null)
            cont.deleteRecord("TEST_USER", userId);
        if (group != null) {
            cont.deleteRecord(group);
        }
    }

    @Test
    public void testTransactions() throws Exception {
        User user;
        Group localGroup;

        // create and persist

        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();

            user = new User();
            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            group = new Group();
            group.setName("group");
            em.persist(group);

            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        // load from DB

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            // find
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            // query
            Query query = em.createQuery("select u from test$User u where u.id = ?1").setParameter(1, userId);
            user = (User) query.getFirstResult();
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(BaseEntityInternalAccess.isNew(localGroup));
            assertTrue(BaseEntityInternalAccess.isManaged(localGroup));
            assertFalse(BaseEntityInternalAccess.isDetached(localGroup));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        assertFalse(BaseEntityInternalAccess.isNew(localGroup));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        user.setName("changed name");

        // merge changed

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.merge(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testSerialization() throws Exception {
        User user;
        Group localGroup;

        // serialize new
        user = new User();
        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));

        user = reserialize(user);

        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));

        // serialize managed

        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();

            group = new Group();
            group.setName("group");
            em.persist(group);

            user = new User();
            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");

            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }

        tx = cont.persistence().createTransaction();

        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.find(User.class, userId);
            assertNotNull(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            localGroup = user.getGroup();
            assertNotNull(localGroup);

            assertFalse(BaseEntityInternalAccess.isNew(localGroup));
            assertTrue(BaseEntityInternalAccess.isManaged(localGroup));
            assertFalse(BaseEntityInternalAccess.isDetached(localGroup));

            user = reserialize(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertTrue(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        user.setName("changed name");

        // merge changed and serialize

        tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            user = em.merge(user);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));

        user = reserialize(user);

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testTransactionRollback_new() throws Exception {
        User user = null;

        // create and persist

        Transaction tx = cont.persistence().createTransaction();
        try {
            user = new User();
            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            userId = user.getId();

            cont.persistence().getEntityManager().persist(user);

            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            tx.commit();

            fail(); // due to absence login
        } catch (Exception e) {
            // ok
        } finally {
            tx.end();
        }

        assertTrue(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertFalse(BaseEntityInternalAccess.isDetached(user));
    }

    @Test
    public void testTransactionRollback_loaded() {
        User user;

        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();

            user = new User();
            assertTrue(BaseEntityInternalAccess.isNew(user));
            assertFalse(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));

            group = new Group();
            group.setName("group");
            em.persist(group);

            userId = user.getId();
            user.setName("testUser");
            user.setLogin("testLogin");
            user.setGroup(group);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }


        tx = cont.persistence().createTransaction();
        try {
            user = cont.persistence().getEntityManager().find(User.class, userId);

            assertFalse(BaseEntityInternalAccess.isNew(user));
            assertTrue(BaseEntityInternalAccess.isManaged(user));
            assertFalse(BaseEntityInternalAccess.isDetached(user));
        } finally {
            tx.end();
        }

        assertFalse(BaseEntityInternalAccess.isNew(user));
        assertFalse(BaseEntityInternalAccess.isManaged(user));
        assertTrue(BaseEntityInternalAccess.isDetached(user));
    }
}