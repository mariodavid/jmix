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
 */

package com.haulmont.cuba.core.model.selfinherited;

import io.jmix.core.metamodel.annotation.Composition;

import javax.persistence.*;
import java.util.List;

@DiscriminatorValue("C")
@Entity(name = "test$ChildEntity")
@Table(name = "TEST_CHILD_ENTITY")
@PrimaryKeyJoinColumn(name = "ENTITY_ID", referencedColumnName = "ID")
public class ChildEntity extends RootEntity {
    private static final long serialVersionUID = 3582114532586946446L;

    @Column(name = "NAME")
    protected String name;

    @OneToMany(mappedBy = "childEntity")
    @Composition
    protected List<ChildEntityDetail> childDetails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
