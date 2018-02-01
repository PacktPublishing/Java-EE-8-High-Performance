package com.github.rmannibucau.failover.cdi.internal;

import com.github.rmannibucau.failover.cdi.api.Failoverable;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FailoverExtension implements Extension {
  private final Map<Class<?>, Collection<Bean<?>>> beans = new HashMap<>();
  private final Annotation failoverableQualifier = new AnnotationLiteral<Failoverable>() {
  };

  void addQualifier(@Observes final BeforeBeanDiscovery beforeBeanDiscovery) {
    beforeBeanDiscovery.addQualifier(Failoverable.class);
  }

  void captureFailoverable(@Observes @WithAnnotations(Failoverable.class) final ProcessAnnotatedType<?> processAnnotatedType) {
    final AnnotatedType<?> annotatedType = processAnnotatedType.getAnnotatedType();
    final Class<?> javaClass = annotatedType.getJavaClass();
    if (javaClass.isInterface() && annotatedType.isAnnotationPresent(Failoverable.class)) {
      getOrCreateImplementationsFor(javaClass);
    }
  }

  void findService(@Observes final ProcessBean<?> processBean) {
    extractFailoverable(processBean)
        .ifPresent(api -> getOrCreateImplementationsFor(api).add(processBean.getBean()));
  }

  void addFailoverableImplementations(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
    beans.forEach((api, implementations) ->
        afterBeanDiscovery.addBean()
          .types(api, Object.class)
          .scope(ApplicationScoped.class)
          .id(Failoverable.class.getName() + "(" + api.getName() + ")")
          .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
          .createWith(ctx -> {
            final Collection<Object> delegates = implementations.stream()
                .sorted(Comparator.comparingInt(b -> getPriority(b, beanManager)))
                .map(b -> beanManager.createInstance()
                    .select(b.getBeanClass(), failoverableQualifier).get())
                .collect(toList());
            final FailoverableHandler handler = new FailoverableHandler(delegates);
            return Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, handler);
          }));
    beans.clear();
  }

  private int getPriority(final Bean<?> bean, final BeanManager beanManager) {
    final AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(bean.getBeanClass());
    return Optional.ofNullable(annotatedType.getAnnotation(Priority.class))
        .map(Priority::value)
        .orElse(1000);
  }

  private Collection<Bean<?>> getOrCreateImplementationsFor(final Class api) {
    return beans.computeIfAbsent(api, i -> new ArrayList<>());
  }

  private Optional<Class> extractFailoverable(final ProcessBean<?> processBean) {
    return processBean.getBean().getQualifiers().contains(failoverableQualifier) ?
        processBean.getBean().getTypes().stream()
          .filter(Class.class::isInstance)
          .map(Class.class::cast)
          .filter(i -> i.isAnnotationPresent(Failoverable.class))
          .flatMap(impl -> Stream.of(impl.getInterfaces()).filter(i -> i != Serializable.class))
          .findFirst() : Optional.empty();
  }
}
