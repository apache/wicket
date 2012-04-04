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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.resolver.IComponentResolver;

/**
 * Markup sourcing strategies determine whether a Component behaves like a "Panel" pulling its
 * Markup from an associated Markup file, or like a Fragment pulling it from a another components
 * Markup.
 * 
 * @author Juergen Donnerstag
 */
public interface IMarkupSourcingStrategy
{
	/**
	 * Will be called in addition to {@link Component#renderHead(HtmlHeaderContainer)} and allows
	 * the strategy to contribute to the &lt;head&gt; section of the response.
	 * 
	 * @see Component#renderHead(HtmlHeaderContainer)
	 * 
	 * @param component
	 *            The component calling the strategy
	 * @param container
	 */
	void renderHead(final Component component, HtmlHeaderContainer container);

	/**
	 * Will be called in addition to {@link Component#onComponentTag(ComponentTag)} and allows the
	 * strategy to modify the component's tag or any of the tag attributes.
	 * 
	 * @see Component#onComponentTag(ComponentTag)
	 * 
	 * @param component
	 *            The component calling the strategy
	 * @param tag
	 */
	void onComponentTag(Component component, ComponentTag tag);

	/**
	 * Will <b>replace</b> the respective component's method.
	 * <p>
	 * It's perfectly valid to call <code>component.onComponentTagBody(markupStream, openTag)</code>
	 * from inside this method.
	 * 
	 * @see Component#onComponentTagBody(MarkupStream, ComponentTag)
	 * 
	 * @param component
	 *            The component calling the strategy
	 * @param markupStream
	 * @param openTag
	 */
	void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag);

	/**
	 * Will <b>replace</b> the respective component's method. However by returning null, the
	 * component's method will be called.
	 * 
	 * @see MarkupContainer#getMarkup(Component)
	 * 
	 * @param container
	 *            The parent containing the child. This is not the direct parent, transparent
	 *            component {@link IComponentResolver resolver} may be in the hierarchy between.
	 * @param child
	 *            The component to find the markup for.
	 * @return the markup fragment for the child, or {@code null}.
	 */
	IMarkupFragment getMarkup(final MarkupContainer container, final Component child);
}
