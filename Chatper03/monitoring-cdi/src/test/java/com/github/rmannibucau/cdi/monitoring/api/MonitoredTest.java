package com.github.rmannibucau.cdi.monitoring.api;

import org.apache.meecrowave.junit.MonoMeecrowave;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MonoMeecrowave.Runner.class)
public class MonitoredTest {
    @Test // adds a spy handler to check the perf log record was published
    public void monitor() {
        final Logger logger = Logger.getLogger("public void com.github.rmannibucau.cdi.monitoring.api.MonitoredService.iAmMonitored()");
        final AtomicReference<LogRecord> record = new AtomicReference<>();
        final Handler spyHandler = new Handler() {
            @Override
            public synchronized void publish(final LogRecord event) {
                record.set(event);
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() throws SecurityException {
                flush();
            }
        };
        logger.addHandler(spyHandler);
        CDI.current().select(MonitoredService.class).get().iAmMonitored();
        logger.removeHandler(spyHandler);

        final LogRecord object = record.get();
        assertNotNull(object);
        assertTrue(object.getMessage(), Pattern.compile("Execution: [0-9]+ ms").matcher(object.getMessage()).matches());
    }
}
