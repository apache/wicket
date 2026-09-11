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

import static org.apache.wicket.markup.parser.XmlTag.TagType.CLOSE;
import static org.apache.wicket.markup.parser.XmlTag.TagType.OPEN;
import static org.apache.wicket.markup.parser.XmlTag.TagType.OPEN_CLOSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class XmlReplacementEnablingBehaviorTest extends WicketTestCase
{
    private static final String SOME_ELEMENT = "someelement";
    private static final String SOME_ID = "some-id";
    private static final String SOME_NAMESPACE_URI = "some-namespace-uri";

    @Test
    void doesNotAcceptNullNamespaceUri()
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new XmlReplacementEnablingBehavior(null));
    }

    @Test
    void addsJavaScriptsNeededForXmlReplacement()
    {
        tester.startPage(XmlReplacementEnablingBehaviorTest.TestPage.class);

        tester.assertContains("<script type=\"text/javascript\" src=\"\\.\\./resource/org\\.apache\\.wicket\\.ajax\\.XmlReplacementMethodResourceReference/res/js/xml-replacement-method\\.js\"></script>");
    }

    @Test
    void doesNotAllowBindingToMultipleComponents()
    {
        var behavior = new XmlReplacementEnablingBehavior(SOME_NAMESPACE_URI);
        behavior.bind(new WebMarkupContainer(SOME_ID));

        Assertions.assertThrows(IllegalStateException.class, () -> behavior.bind(new WebMarkupContainer(SOME_ID)));
    }

    @Test
    void enablesOutputOfMarkupIdOnBindingToComponent()
    {
        var behavior = new XmlReplacementEnablingBehavior(SOME_NAMESPACE_URI);
        var component = new WebMarkupContainer(SOME_ID);
        behavior.bind(component);

        Assertions.assertTrue(component.getOutputMarkupId());
    }

    @Test
    void setsNamespaceUriOnComponentOpenTag()
    {
        var behavior = new XmlReplacementEnablingBehavior("http://example.com/the-namespace-uri");
        var tag = new ComponentTag(SOME_ELEMENT, OPEN);

        behavior.onComponentTag(new WebMarkupContainer(SOME_ID), tag);

        assertEquals("http://example.com/the-namespace-uri", tag.getAttribute("xmlns"));
    }

    @Test
    void setsNamespaceUriOnComponentOpenCloseTag()
    {
        var behavior = new XmlReplacementEnablingBehavior("http://example.com/the-namespace-uri");
        var tag = new ComponentTag(SOME_ELEMENT, OPEN_CLOSE);

        behavior.onComponentTag(new WebMarkupContainer(SOME_ID), tag);

        assertEquals("http://example.com/the-namespace-uri", tag.getAttribute("xmlns"));
    }

    @Test
    void doesNotSetNamespaceUriOnComponentCloseTag()
    {
        var behavior = new XmlReplacementEnablingBehavior("http://example.com/the-namespace-uri");
        var tag = new ComponentTag(SOME_ELEMENT, CLOSE);

        behavior.onComponentTag(new WebMarkupContainer(SOME_ID), tag);

        assertNull(tag.getAttribute("xmlns"));
    }

    @Test
    void disablesRenderingOfWicketTags()
    {
        tester.startPage(XmlReplacementEnablingBehaviorTest.TestPage.class);

        tester.assertContainsNot("<wicket:panel>");
    }

    public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * Construct.
         */
        public TestPage()
        {
            add(new MathmlSubexpressionPanel("component")
                    .add(new XmlReplacementEnablingBehavior(XmlReplacementEnablingBehavior.MATHML_NAMESPACE_URI)));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container,
                                                       Class<?> containerClass)
        {
            return new StringResourceStream(
                    "<html><head></head><body><math xmlns=\"http://www.w3.org/1998/Math/MathML\"><mrow wicket:id=\"component\"></mrow></math></body></html>");
        }
    }
}
