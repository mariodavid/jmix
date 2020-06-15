package io.jmix.core.constraint;

public interface AccessConstraint<T extends AccessContext> {

    Class<T> getContextType();

    void applyTo(T context);
}
