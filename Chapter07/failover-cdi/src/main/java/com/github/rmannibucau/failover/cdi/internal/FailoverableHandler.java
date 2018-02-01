package com.github.rmannibucau.failover.cdi.internal;

import com.github.rmannibucau.failover.cdi.api.Failoverable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

class FailoverableHandler implements InvocationHandler {
    private final Collection<Object> delegates;

    FailoverableHandler(final Collection<Object> implementations) {
        this.delegates = implementations;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        for (final Object delegate : delegates) {
            try {
                return method.invoke(delegate, args);
            } catch (final InvocationTargetException ite) {
                final Throwable targetException = ite.getTargetException();
                if (supportsFailover(targetException)) {
                    continue;
                }
                throw targetException;
            }
        }
        throw new FailoverException("No success for " + method + " between " + delegates.size() + " services");
    }

    private boolean supportsFailover(final Throwable targetException) {
        return targetException.getClass().isAnnotationPresent(Failoverable.class);
    }
}
