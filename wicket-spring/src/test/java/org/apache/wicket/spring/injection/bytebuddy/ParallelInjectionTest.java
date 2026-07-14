/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.spring.injection.bytebuddy;

import org.apache.wicket.Page;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* WICKET-7005 */
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ParallelInjectionTest.ServiceConfig.class })
public class ParallelInjectionTest implements ApplicationContextAware {

    @ComponentScan(basePackages = "org.apache.wicket.spring.injection.bytebuddy")
    @Configuration
    public static class ServiceConfig {
    }

    @Named
    public static class ServiceA {
        @Inject
        private ServiceC serviceC;
        @Inject
        private ServiceD serviceD;
    }

    @Named
    public static class ServiceB {
    }

    @Named
    public static class ServiceC {
    }

    @Named
    public static class ServiceD {
    }

    public static class DemoComponent {

        @Inject
        private ServiceB serviceB;
        @Inject
        private ServiceA serviceA;

        public DemoComponent() {
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ParallelInjectionTest.class);

    private ApplicationContext context;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("wicket.ioc.useByteBuddy", Boolean.TRUE.toString());
    }

    @Test
    void parallel() throws Exception {
        runInjection(10);
    }

    @Test
    void notParallel() throws Exception {
        runInjection(1);
    }

    void runInjection(int nThreads) throws InterruptedException {
                // Arrange
        var tester = new WicketTester(createPortalApplication());
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);

        Callable<String> callableTask = () -> {
            LOG.debug("Thread id: {}", Thread.currentThread().getId());
            ThreadContext.setApplication(tester.getApplication());
            var panel = new DemoComponent();
            Injector.get().inject(panel);
            return "Injected";
        };

        // Act
        var tasks = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> callableTask)
                .collect(Collectors.toList());

        var futures = executor.invokeAll(tasks);

        futures.forEach(f -> {
            try {
                LOG.debug("Returned {}", f.get());
            } catch (Exception e) {
                throw new RuntimeException("A problem occurred", e);
            }
        });
    }

    private WebApplication createPortalApplication() {
        LOG.debug("Erstelle MockServletContext mit applicationContext");

        MockServletContext mockServletContext = new MockServletContext();

        mockServletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);

        LOG.debug("Erstelle PortalApplication ...");

        return new WebApplication() {
            @Override
            public ServletContext getServletContext() {
                return mockServletContext;
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return DummyHomePage.class;
            }

            @Override
            protected void init() {
                super.init();

                getComponentInstantiationListeners().add(new SpringComponentInjector(this));
                getCspSettings().blocking().disabled();
            }
        };
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
