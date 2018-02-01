package com.github.rmannibucau.jmh.junit;

import com.github.rmannibucau.jmh.junit.api.ExpectedPerformances;
import org.apache.meecrowave.Meecrowave;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import javax.enterprise.inject.spi.CDI;

@RunWith(JMHRunner.class)
public class JMHRunnerDemo {

    // here is the first benchmark,
    // you can define as much as nested "benchmark" classes
    // as you want
    public static class QuoteMicrobenchmark {
        // you can define as much @Benchmark method you want in a benchmark class
        @Benchmark
        // tune these numbers depending your expected clients count/behavior
        @Fork(1)
        @Threads(2)
        @Warmup(iterations = 10)
        @Measurement(iterations = 100)
        // whatever performance target you have for the box executing the test
        @ExpectedPerformances(score = 5.0000000000000000E8, scoreTolerance = 0.25)
        public void findByName(final QuoteState quoteState, final Blackhole blackhole) {
            blackhole.consume(quoteState.service.findByName("test"));
        }

        @State(Scope.Benchmark)
        public static class QuoteState {
            private QuoteService service;
            private Meecrowave server;

            @Setup
            public void setup() {
                server = new Meecrowave(new Meecrowave.Builder().randomHttpPort()).bake();
                service = CDI.current().select(QuoteService.class).get();
            }

            @TearDown
            public void teardown() {
                server.close();
            }
        }
    }

}
