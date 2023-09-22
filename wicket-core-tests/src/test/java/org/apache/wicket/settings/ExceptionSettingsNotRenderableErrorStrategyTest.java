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
package org.apache.wicket.settings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ExceptionSettings#notRenderableErrorStrategy}
 */
class ExceptionSettingsNotRenderableErrorStrategyTest extends WicketTestCase {

    private WicketTagTestPage wicketTagTestPage;
    private RenderBodyOnlyTestPage renderBodyOnlyTestPage;

    @BeforeEach
    void before()
    {
        ExceptionSettings exceptionSettings = tester.getApplication().getExceptionSettings();
        exceptionSettings.setNotRenderableErrorStrategy(ExceptionSettings.NotRenderableErrorStrategy.THROW_EXCEPTION);

        wicketTagTestPage = new WicketTagTestPage();
        renderBodyOnlyTestPage = new RenderBodyOnlyTestPage();
    }

    @Test
    void notRenderableErrorStrategy_whenWicketTagAndOutputId_throwException() {
        try
        {
            wicketTagTestPage.component.setOutputMarkupId(true);
            startWicketTagPage();
        } catch (WicketRuntimeException wrx)
        {
            assertWicketTagException(wrx);
        }
    }

    @Test
    void notRenderableErrorStrategy_whenWicketTagAndOutputPlaceholder_throwException() {
        try
        {
            wicketTagTestPage.component.setOutputMarkupPlaceholderTag(true);
            startWicketTagPage();
        } catch (WicketRuntimeException wrx)
        {
            assertWicketTagException(wrx);
        }
    }

    @Test
    void notRenderableErrorStrategy_whenRenderBodyOnlyAndOutputId_throwException() {
        try
        {
            renderBodyOnlyTestPage.component.setOutputMarkupId(true);
            startRenderBodyOnlyPage();
        } catch (WicketRuntimeException wrx)
        {
            assertRenderBodyOnlyException(wrx);
        }
    }

    @Test
    void notRenderableErrorStrategy_whenRenderBodyOnlyAndOutputPlaceholder_throwException() {
        try
        {
            renderBodyOnlyTestPage.component.setOutputMarkupPlaceholderTag(true);
            startRenderBodyOnlyPage();
        } catch (WicketRuntimeException wrx)
        {
            assertRenderBodyOnlyException(wrx);
        }
    }

    private void startWicketTagPage() {
        tester.startPage(wicketTagTestPage);
        fail("Should have thrown exception!");
    }

    private void startRenderBodyOnlyPage() {
        tester.startPage(renderBodyOnlyTestPage);
        fail("Should have thrown exception!");
    }

    private void assertWicketTagException(WicketRuntimeException wrx) {
        assertThat(wrx.getCause().getMessage(), is(equalTo("Markup id set on a component that is usually not "
                                                           + "rendered into markup. Markup id: test1, component id: test, "
                                                           + "component tag: wicket:label.")));
    }

    private void assertRenderBodyOnlyException(final WicketRuntimeException wrx) {
        assertThat(wrx.getCause().getMessage(), is(equalTo("Markup id set on a component that renders its "
                                                           + "body only. Markup id: test1, component id: test.")));
    }

    private static class WicketTagTestPage extends WebPage implements IMarkupResourceStreamProvider
    {
        private Label component;

        private WicketTagTestPage() {
            component = new Label("test", "Test");
            add(component);
        }

        @Override
        public IResourceStream getMarkupResourceStream(final MarkupContainer container, final Class<?> containerClass) {
            return new StringResourceStream("<html><body><wicket:label wicket:id='test'/></body></html>");
        }
    }

    private static class RenderBodyOnlyTestPage extends WebPage implements IMarkupResourceStreamProvider
    {
        private Label component;

        private RenderBodyOnlyTestPage() {
            component = new Label("test", "Test");
            component.setRenderBodyOnly(true);
            add(component);
        }

        @Override
        public IResourceStream getMarkupResourceStream(final MarkupContainer container, final Class<?> containerClass) {
            return new StringResourceStream("<html><body><span wicket:id='test'></span></body></html>");
        }
    }
}
