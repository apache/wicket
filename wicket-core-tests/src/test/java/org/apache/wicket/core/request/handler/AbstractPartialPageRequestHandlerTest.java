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
package org.apache.wicket.core.request.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.page.PartialPageUpdate;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class AbstractPartialPageRequestHandlerTest extends WicketTestCase
{
    @Test
    void passesNoReplacementMethodToUpdateForComponentWithOverriddenMarkupIdRegisteredWithout()
    {
        Label label = tester.startComponentInPage(new Label("some ID"));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(label.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(label.getPage(), update);

        handler.add(label, "the ID");

        assertNull(update.getReplacementMethodPublic(label.getMarkupId()));
    }

    @Test
    void passesNoReplacementMethodToUpdateForComponentRegisteredWithout()
    {
        Component label = tester.startComponentInPage(new Label("the ID").setOutputMarkupId(true));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(label.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(label.getPage(), update);

        handler.add(label);

        assertNull(update.getReplacementMethodPublic(label.getMarkupId()));
    }

    @Test
    void passesNoReplacementMethodToUpdateForChildrenRegisteredWithout()
    {
        Component label = new Label("the ID").setOutputMarkupId(true).setMarkup(Markup.of("<span wicket:id=\"the ID\"></span>"));
        MarkupContainer container = new WebMarkupContainer("some ID").add(label);
        tester.startComponentInPage(container.setMarkup(Markup.of("<span wicket:id=\"some ID\"><span wicket:id=\"the ID\"></span></span>")));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(container.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(container.getPage(), update);

        handler.addChildren(container, Label.class);

        assertNull(update.getReplacementMethodPublic(label.getMarkupId()));
    }

    @Test
    void passesReplacementMethodToUpdateForComponentWithOverriddenMarkupIdRegisteredWithOne()
    {
        Label label = tester.startComponentInPage(new Label("some ID"));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(label.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(label.getPage(), update);

        handler.add("theReplacementMethod", label, "the ID");

        assertEquals("theReplacementMethod", update.getReplacementMethodPublic(label.getMarkupId()));
    }

    @Test
    void passesReplacementMethodToUpdateForComponentRegisteredWithOne()
    {
        Component label = tester.startComponentInPage(new Label("the ID").setOutputMarkupId(true));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(label.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(label.getPage(), update);

        handler.add("theReplacementMethod", label);

        assertEquals("theReplacementMethod", update.getReplacementMethodPublic(label.getMarkupId()));
    }

    @Test
    void passesReplacementMethodToUpdateForChildrenRegisteredWithOne()
    {
        Component label = new Label("the ID").setOutputMarkupId(true).setMarkup(Markup.of("<span wicket:id=\"the ID\"></span>"));
        MarkupContainer container = new WebMarkupContainer("some ID").add(label);
        tester.startComponentInPage(container.setMarkup(Markup.of("<span wicket:id=\"some ID\"><span wicket:id=\"the ID\"></span></span>")));
        ReplacementMethodsExposingUpdate update = new ReplacementMethodsExposingUpdate(container.getPage());
        AbstractPartialPageRequestHandler handler = new TestPartialPageRequestHandler(container.getPage(), update);

        handler.addChildren("theReplacementMethod", container, Label.class);

        assertEquals("theReplacementMethod", update.getReplacementMethodPublic(label.getMarkupId()));
    }

    private static class ReplacementMethodsExposingUpdate extends XmlPartialPageUpdate
    {
        public ReplacementMethodsExposingUpdate(Page page)
        {
            super(page);
        }

        public String getReplacementMethodPublic(String markupId) {
            return getReplacementMethod(markupId);
        }
    }

    private static class TestPartialPageRequestHandler extends AbstractPartialPageRequestHandler
    {
        private final ReplacementMethodsExposingUpdate update;

        public TestPartialPageRequestHandler(Page page, ReplacementMethodsExposingUpdate update)
        {
            super(page);
            this.update = update;
        }

        @Override
        protected PartialPageUpdate getUpdate()
        {
            return update;
        }

        @Override
        public Collection<? extends Component> getComponents()
        {
            return List.of();
        }

        @Override
        public void respond(IRequestCycle requestCycle)
        {
        }
    }
}
