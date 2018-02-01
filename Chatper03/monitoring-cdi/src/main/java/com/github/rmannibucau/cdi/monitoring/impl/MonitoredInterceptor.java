package com.github.rmannibucau.cdi.monitoring.impl;

import com.github.rmannibucau.cdi.monitoring.api.Monitored;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Interceptor
@Monitored
@Priority(Interceptor.Priority.PLATFORM_BEFORE)
public class MonitoredInterceptor implements Serializable {
    @AroundInvoke
    @AroundTimeout
    public Object monitor(final InvocationContext invocationContext) throws Exception {
        final Context ctx = newContext(invocationContext);
        Exception error = null;
        try {
            return invocationContext.proceed();
        } catch (final Exception t) {
            error = t;
            throw t;
        } finally {
            if (error == null) {
                ctx.stop();
            } else {
                ctx.stopWithException(error);
            }
        }
    }

    private Context newContext(final InvocationContext invocationContext) {
        return new Context(invocationContext.getMethod().toGenericString());
    }

    // simple context implementation using a plain logger
    private static class Context {
        private final String method;
        private final long start;
        private Exception error;
        private long end;

        private Context(final String method) {
            this.method = method;
            this.start = System.nanoTime();
        }

        private void stop() {
            end = System.nanoTime();
            doLog();
        }

        private void stopWithException(final Exception error) {
            stop();
            this.error = error;
            doLog();
        }

        private void doLog() {
            final Logger logger = Logger.getLogger(method);
            logger.info("Execution: " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms" +
                            (error != null ? ", error: " + error.getMessage() : ""));
        }
    }
}
