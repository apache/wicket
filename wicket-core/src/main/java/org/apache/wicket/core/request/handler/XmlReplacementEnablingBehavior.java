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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.XmlReplacementMethodResourceReference;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.util.lang.Args;

/**
 * Add this behavior to components that are attached to XML markup in the page. This is usually used for components
 * attached SVG or MathML elements.
 * <p>
 * The behavior will set the given namespace on the component tag so it is correctly added to the DOM, ensuring the
 * browser renders the new markup correctly.
 * <p>
 * As this behavior implies that the component will be added to a
 * {@link org.apache.wicket.page.PartialPageUpdate PartialPageUpdate}, this behavior will enable outputting of the
 * markup ID of the component. Calling {@link Component#setOutputMarkupId(boolean) setOutputMarkupId(...)} yourself is
 * not needed.
 * <p>
 * Limitations:
 * <ul>
 *     <li>
 *         The replaced XML will not be rendered correctly if it contains Wicket tags. To prevent rendering problems
 *         Wicket tags are always stripped for components to which this behavior is added.
 *     </li>
 * </ul>
 * Make sure you test that markup changes are properly applied for your situation. There is no comprehensive set of
 * tests to check if this replacement method works exactly the same as the standard method using jQuery.
 */
public class XmlReplacementEnablingBehavior extends Behavior
{
    /**
     * The namespace URI of MathML.
     */
    public static final String MATHML_NAMESPACE_URI = "http://www.w3.org/1998/Math/MathML";

    /**
     * The namespace URI of SVG.
     */
    public static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

    /**
     * The identifier to pass to {@link IPartialPageRequestHandler#add(String, Component, String)},
     * {@link IPartialPageRequestHandler#add(String, Component...)} or
     * {@link IPartialPageRequestHandler#addChildren(String, MarkupContainer, Class)} to have the markup of the
     * component replaced using the XML replacement method.
     */
    public static final String XML = "xml";

    private static final HeaderItem XML_REPLACEMENT_METHOD_HEADER_ITEM =
            JavaScriptHeaderItem.forReference(XmlReplacementMethodResourceReference.get());

    private final String namespaceUri;

    private boolean hasBeenBound;

    private transient boolean previousStripWicketTags;

    /**
     * Create a new instance with the given namespace URI. Instances cannot be shared between components.
     *
     * @param namespaceUri the namespace URI of the element the component is attached to.
     */
    public XmlReplacementEnablingBehavior(String namespaceUri)
    {
        Args.notNull(namespaceUri, "namespaceUri");
        this.namespaceUri = namespaceUri;
    }

    @Override
    public void bind(Component component)
    {
        if (hasBeenBound) {
            throw new IllegalStateException("this kind of behavior cannot be attached to multiple components");
        }
        hasBeenBound = true;
        component.setOutputMarkupId(true);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response)
    {
        response.render(XML_REPLACEMENT_METHOD_HEADER_ITEM);
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag)
    {
        if (!tag.isClose()) {
            tag.put("xmlns", namespaceUri);
        }
    }

    @Override
    public void beforeRender(Component component)
    {
        var markupSettings = component.getApplication().getMarkupSettings();
        previousStripWicketTags = markupSettings.getStripWicketTags();
        markupSettings.setStripWicketTags(true);
    }

    @Override
    public void afterRender(Component component)
    {
        component.getApplication().getMarkupSettings().setStripWicketTags(previousStripWicketTags);
    }
}
