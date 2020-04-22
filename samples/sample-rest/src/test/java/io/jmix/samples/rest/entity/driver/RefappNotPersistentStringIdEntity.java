/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.entity.driver;

import io.jmix.core.entity.BaseStringIdEntity;
import io.jmix.core.metamodel.annotations.MetaClass;
import io.jmix.core.metamodel.annotations.MetaProperty;
import io.jmix.core.metamodel.annotations.NamePattern;

@NamePattern("%s|name")
@MetaClass(name = "jmix$RefappNotPersistentStringIdEntity")
public class RefappNotPersistentStringIdEntity extends BaseStringIdEntity {

    @MetaProperty
    protected String id;

    @MetaProperty
    protected String name;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
