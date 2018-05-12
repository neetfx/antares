/*
 * Copyright 2018 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.prchen.antares.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

@Component
class AntaresRegistry implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor, PriorityOrdered, AntaresContext {
    private Logger logger = LoggerFactory.getLogger(AntaresRegistry.class);
    private ApplicationContext context;
    private String[] basePackages;
    private Class<? extends Annotation>[] manifestAdvices;
    private Set<Class<?>> manifest;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        try {
            initBasePackages(registry);
            if (basePackages.length > 0) {
                initManifestAdvises();
                initFactories(registry);
                initManifest();
            }
        } catch (RuntimeException e) {
            logger.error("Failed to process BeanDefinitionRegistry", e);
            throw e;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {

    }

    @Override
    public Set<Class<?>> getManifest(Class<? extends Annotation> advice) {
        return manifest.stream()
                .filter(x -> x.getAnnotation(advice) != null)
                .collect(Collectors.toSet());
    }

    private void initBasePackages(BeanDefinitionRegistry registry) {
        if (basePackages != null) {
            return;
        }
        List<String> result = new LinkedList<>();
        for (String name : registry.getBeanDefinitionNames()) {
            String className = registry.getBeanDefinition(name).getBeanClassName();
            if (className != null) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.getAnnotation(AntaresScan.class) != null) {
                        result.add(clazz.getPackage().getName());
                    }
                } catch (ClassNotFoundException e) {
                    logger.info("Skipping not loaded bean class: " + className);
                }
            }
        }
        basePackages = result.toArray(new String[0]);
        if (basePackages.length > 0) {
            logger.info("Base packages: " + String.join(", ", basePackages));
        } else {
            logger.info("Base packages not set");
        }
    }

    @SuppressWarnings("unchecked")
    private void initManifestAdvises() {
        Set<Class<?>> types = new CandidateScanner(
                Collections.singletonList(Annotation.class),
                Collections.singletonList(AntaresManifestAdvice.class))
                .scanTypes();
        manifestAdvices = types.stream()
                .map(x -> (Class<? extends Annotation>) x)
                .collect(Collectors.toList())
                .toArray(new Class[0]);
    }

    private void initFactories(BeanDefinitionRegistry registry) {
        for (Class<? extends Annotation> advise : manifestAdvices) {
            Class<?> factoryClass = advise.getAnnotation(AntaresManifestAdvice.class).factoryClass();
            if (factoryClass != AntaresFactoryBean.class) {
                logger.info("Loading factory: " + factoryClass.getName());
                new DefinitionScanner(registry, advise).scan(basePackages);
            }
        }
    }

    private void initManifest() {
        manifest = new CandidateScanner(
                Collections.emptyList(),
                Arrays.asList(manifestAdvices))
                .scanTypes();
        if (manifest.size() > 0) {
            String manifest = String.join(", ", this.manifest.stream()
                    .map(Class::getName)
                    .collect(Collectors.toList()));
            logger.info("Antares manifest: " + manifest);
        } else {
            logger.info("Empty Antares manifest detected");
        }
    }

    class DefinitionScanner extends ClassPathBeanDefinitionScanner {
        private Class<?> factory;

        private DefinitionScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> advise) {
            super(registry, false);
            super.setResourceLoader(context);
            super.setBeanNameGenerator((d, r) -> StringUtils.uncapitalize(d.getBeanClassName()));
            this.factory = advise.getAnnotation(AntaresManifestAdvice.class).factoryClass();
            addIncludeFilter(new AnnotationTypeFilter(advise, false));
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return super.isCandidateComponent(beanDefinition) ||
                    (beanDefinition.getMetadata().isInterface() &&
                            beanDefinition.getMetadata().isIndependent());
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
            for (BeanDefinitionHolder definition : beanDefinitions) {
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) definition.getBeanDefinition();
                String className = beanDefinition.getBeanClassName();
                AntaresRegistry.this.logger.info("Registering bean: " + className);
                try {
                    Class<?> beanClass = Class.forName(className);
                    beanDefinition.setBeanClass(factory);
                    beanDefinition.getPropertyValues().add("objectType", beanClass);
                } catch (ClassNotFoundException e) {
                    AntaresRegistry.this.logger.warn("Failed to load class: " + className);
                }
            }
            return beanDefinitions;
        }

    }

    class CandidateScanner extends ClassPathScanningCandidateComponentProvider {

        private CandidateScanner(List<Class<?>> types, List<Class<? extends Annotation>> annotations) {
            super(false);
            for (Class<?> type : types) {
                addIncludeFilter(new AssignableTypeFilter(type));
            }
            for (Class<? extends Annotation> annotation : annotations) {
                addIncludeFilter(new AnnotationTypeFilter(annotation, false));
            }
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return super.isCandidateComponent(beanDefinition) ||
                    (beanDefinition.getMetadata().isInterface() &&
                            beanDefinition.getMetadata().isIndependent());
        }

        Set<Class<?>> scanTypes() {
            Set<String> classNames = new HashSet<>();
            for (String basePackage : basePackages) {
                findCandidateComponents(basePackage).stream()
                        .map(BeanDefinition::getBeanClassName)
                        .forEach(classNames::add);
            }
            Set<Class<?>> result = new HashSet<>();
            for (String name : classNames) {
                try {
                    result.add(Class.forName(name));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return result;
        }

    }
}
