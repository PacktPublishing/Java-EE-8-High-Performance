package com.github.rmannibucau.jmh.junit;

import com.github.rmannibucau.jmh.junit.api.ExpectedPerformances;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JMHRunner extends ParentRunner<Class<?>> {
    private List<Class<?>> children;

    public JMHRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<Class<?>> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(final Class<?> child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.getSimpleName());
    }

    @Override
    protected void collectInitializationErrors(final List<Throwable> errors) {
        super.collectInitializationErrors(errors);

        children = Stream.of(getTestClass().getJavaClass().getClasses())
                .filter(benchmarkClass -> Stream.of(benchmarkClass.getMethods())
                        .anyMatch(m -> m.isAnnotationPresent(Benchmark.class)))
                .collect(toList());

        errors.addAll(children.stream()
                .flatMap(c -> Stream.of(c.getMethods())
                        .filter(m -> m.isAnnotationPresent(Benchmark.class)))
                .filter(m -> !m.isAnnotationPresent(ExpectedPerformances.class))
                .map(m -> new IllegalArgumentException("No @ExpectedPerformances on " + m))
                .collect(toList()));
    }

    @Override
    protected boolean isIgnored(final Class<?> child) {
        return child.isAnnotationPresent(Ignore.class);
    }

    @Override
    protected void runChild(final Class<?> child, final RunNotifier notifier) {
        final Description description = describeChild(child);
        if (isIgnored(child)) {
            notifier.fireTestIgnored(description);
        } else {
            runLeaf(benchmarkStatement(child), description, notifier);
        }
    }

    private Statement benchmarkStatement(final Class<?> benchmarkClass) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final Collection<RunResult> results;
                try {
                    results = new Runner(buildOptions(benchmarkClass)).run();
                } catch (final RunnerException e) {
                    throw new IllegalStateException(e);
                }

                assertResults(benchmarkClass, results);
            }
        };
    }

    // all options will use JMH annotations so just include the class to run
    private Options buildOptions(final Class<?> test) {
        return new OptionsBuilder()
                .include(test.getName().replace('$', '.'))
                .build();
    }

    private void assertResults(final Class<?> benchmarkClass, final Collection<RunResult> results) {
        // for all benchmarks assert the performances from the results
        final List<AssertionError> errors = Stream.of(benchmarkClass.getMethods())
                .filter(m -> m.isAnnotationPresent(Benchmark.class))
                .map(m -> {
                    final Optional<RunResult> methodResult = results.stream()
                            .filter(r -> m.getName().equals(r.getPrimaryResult().getLabel()))
                            .findFirst();
                    assertTrue(m + " didn't get any result", methodResult.isPresent());

                    final ExpectedPerformances expectations = m.getAnnotation(ExpectedPerformances.class);
                    final RunResult result = results.iterator().next();
                    final BenchmarkResult aggregatedResult = result.getAggregatedResult();

                    final double actualScore = aggregatedResult.getPrimaryResult().getScore();
                    final double expectedScore = expectations.score();
                    final double acceptedError = expectedScore * expectations.scoreTolerance();
                    try { // use assert to get a common formatting for errors
                        assertEquals(m.getDeclaringClass().getSimpleName() + "#" + m.getName(), expectedScore, actualScore, acceptedError);
                        return null;
                    } catch (final AssertionError ae) {
                        return ae;
                    }
                }).filter(Objects::nonNull).collect(toList());
        if (!errors.isEmpty()) {
            throw new AssertionError(errors.stream()
                    .map(Throwable::getMessage)
                    .collect(joining("\n")));
        }
    }
}
