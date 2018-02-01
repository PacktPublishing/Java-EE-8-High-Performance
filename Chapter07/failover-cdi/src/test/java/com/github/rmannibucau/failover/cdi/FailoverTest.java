package com.github.rmannibucau.failover.cdi;

import com.github.rmannibucau.failover.cdi.api.Failoverable;
import org.apache.meecrowave.junit.MeecrowaveRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FailoverTest {
    @ClassRule
    public static final MeecrowaveRule MEECROWAVE = new MeecrowaveRule();

    @Inject
    private Facade facade;

    @Inject
    @Failoverable
    private FacadeImpl1 facadeImpl1;

    @Test
    public void run() {
        MEECROWAVE.inject(this);
        assertFalse(facadeImpl1.isCalled());
        assertEquals("ok", facade.call());
        assertTrue(facadeImpl1.isCalled());
    }

    public interface Facade {
        String call();
    }

    @Priority(0)
    @Failoverable
    @ApplicationScoped
    public static class FacadeImpl1 implements Facade {
        private boolean called;

        public boolean isCalled() {
            return called;
        }

        @Override
        public String call() {
            called = true;
            throw new Failed();
        }
    }

    @Priority(1)
    @Failoverable
    @ApplicationScoped
    public static class FacadeImpl2 implements Facade {
        @Override
        public String call() {
            return "ok";
        }
    }

    @Failoverable
    private static class Failed extends RuntimeException {
    }
}
