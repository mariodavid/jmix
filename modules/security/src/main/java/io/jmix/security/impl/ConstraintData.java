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

package io.jmix.security.impl;


import io.jmix.core.security.ConstraintOperationType;
import io.jmix.security.entity.ConstraintCheckType;

import java.io.Serializable;
import java.util.UUID;

public class ConstraintData implements Serializable {
    protected final UUID id;
    protected final String code;
    protected final ConstraintOperationType operationType;
    protected final ConstraintCheckType checkType;
    protected final String join;
    protected final String whereClause;
    protected final String groovyScript;

    public ConstraintData(UUID id, String code,
                          ConstraintOperationType operationType,
                          ConstraintCheckType checkType,
                          String join, String whereClause, String groovyScript) {
        this.id = id;
        this.code = code;
        this.operationType = operationType;
        this.checkType = checkType;
        this.join = join;
        this.whereClause = whereClause;
        this.groovyScript = groovyScript;
    }

    //    public ConstraintData(Constraint constraint) {
//        this.id = constraint.getId();
//        this.code = constraint.getCode();
//        this.join = constraint.getJoinClause();
//        this.whereClause = constraint.getWhereClause();
//        this.groovyScript = constraint.getGroovyScript();
//        this.operationType = constraint.getOperationType();
//        this.checkType = constraint.getCheckType();
//    }

    public String getCode() {
        return code;
    }

    public String getJoin() {
        return join;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public ConstraintOperationType getOperationType() {
        return operationType;
    }

    public ConstraintCheckType getCheckType() {
        return checkType;
    }

    public UUID getId() {
        return id;
    }
}