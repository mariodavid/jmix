package io.jmix.data.impl.context;

import io.jmix.core.Metadata;
import io.jmix.core.QueryTransformerFactory;
import io.jmix.core.constraint.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.impl.JmixQuery;

/**
 * Modifies the query depending on current security constraints.
 */
public class JpqlQueryContext implements AccessContext {
    protected final JmixQuery<?> originalQuery;
    protected final MetaClass entityClass;
    protected boolean constrainsApplied;

    protected Metadata metadata;
    protected QueryTransformerFactory;

    public JpqlQueryContext(JmixQuery<?> originalQuery) {
        this.originalQuery = originalQuery;
    }

    protected MetaClass getEntityClass() {
        return entityClass;
    }

    public void addWhereWithJoin(String where, String join) {
        transformer.addWhereAndJoin(where, join);
    }

    public JmixQuery<?> getResultQuery() {
        return transformer.getResult();
    }

    public boolean isConstrainsApplied() {
        return constrainsApplied;
    }
}
