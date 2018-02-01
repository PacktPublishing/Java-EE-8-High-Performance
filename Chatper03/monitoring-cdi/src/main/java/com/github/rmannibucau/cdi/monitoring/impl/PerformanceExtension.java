package com.github.rmannibucau.cdi.monitoring.impl;

import com.github.rmannibucau.cdi.monitoring.api.Monitored;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PerformanceExtension implements Extension {
    private final Annotation monitored = new AnnotationLiteral<Monitored>() {};
    private final Properties configuration = new Properties();
    private boolean enabled;

    void loadConfiguration(@Observes final BeforeBeanDiscovery beforeBeanDiscovery) {
        try (final InputStream configStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("performances.properties")) {
            if (configStream != null) {
                configuration.load(configStream);
            }
        } catch (final IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }
        enabled = Boolean.parseBoolean(configuration.getProperty("enabled", "true"));
    }

    <A> void processAnnotatedType(@Observes final ProcessAnnotatedType<A> pat) {
        if (!enabled) {
            return;
        }

        final String beanClassName = pat.getAnnotatedType().getJavaClass().getName();
        if (Boolean.parseBoolean(configuration.getProperty(beanClassName + ".monitor", "false"))) {
            pat.setAnnotatedType(new WrappedAnnotatedType<>(pat.getAnnotatedType(), monitored));
        }
    }

    private static class WrappedAnnotatedType<A> implements AnnotatedType<A> {
        private final AnnotatedType<A> delegate;
        private final Set<Annotation> annotations;

        public WrappedAnnotatedType(final AnnotatedType<A> at, final Annotation additionalAnnotation) {
            this.delegate = at;

            this.annotations = new HashSet<>(at.getAnnotations().size() + 1);
            this.annotations.addAll(at.getAnnotations());
            this.annotations.add(additionalAnnotation);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        @Override
        public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
            for (final Annotation ann : annotations) {
                if (ann.annotationType() == annotationType) {
                    return annotationType.cast(ann);
                }
            }
            return null;
        }

        @Override
        public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
            return getAnnotation(annotationType) != null;
        }

        @Override
        public Class<A> getJavaClass() {
            return delegate.getJavaClass();
        }

        @Override
        public Set<AnnotatedConstructor<A>> getConstructors() {
            return delegate.getConstructors();
        }

        @Override
        public Set<AnnotatedMethod<? super A>> getMethods() {
            return delegate.getMethods();
        }

        @Override
        public Set<AnnotatedField<? super A>> getFields() {
            return delegate.getFields();
        }

        @Override
        public <T extends Annotation> Set<T> getAnnotations(final Class<T> annotationType) {
            return delegate.getAnnotations(annotationType);
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }
    }
}
