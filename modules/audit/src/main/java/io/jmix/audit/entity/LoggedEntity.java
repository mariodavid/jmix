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

package io.jmix.audit.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.Creatable;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.data.entity.BaseUuidEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.Set;

/**
 * Configuration element of <code>EntityLog</code> bean.
 */
@Entity(name = "audit_LoggedEntity")
@Table(name = "AUDIT_LOGGED_ENTITY")
@SystemLevel
public class LoggedEntity extends BaseUuidEntity implements Creatable {

    private static final long serialVersionUID = 2189206984294705835L;

    @Column(name = "CREATE_TS")
    private Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "AUTO")
    private Boolean auto;

    @Column(name = "MANUAL")
    private Boolean manual;

    @OneToMany(mappedBy = "entity")
    @OnDelete(DeletePolicy.CASCADE)
    private Set<LoggedAttribute> attributes;

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAuto() {
        return auto;
    }

    public void setAuto(Boolean auto) {
        this.auto = auto;
    }

    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    public Set<LoggedAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<LoggedAttribute> attributes) {
        this.attributes = attributes;
    }
}
