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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;

/**
 * The Panel components markup sourcing strategy.
 * <p>
 * The strategy supports two modes on how to handle the body markup. A typical Panel will ignore the
 * body markup and replace it with the associated markup. The body markup is allowed to have raw
 * markup only and no Wicket components. But e.g. a Border component will associate the body markup
 * with a Body component which renders the markup including all any number of child Components.
 * 
 * @author Juergen Donnerstag
 */
public class PanelMarkupSourcingStrategy extends AssociatedMarkupSourcingStrategy
{
	// False for Panel and true for Border components.
	private final boolean allowWicketComponentsInBodyMarkup;

	/**
	 * Constructor.
	 * 
	 * @param wicketTagName
	 *            The tag name for <code>&lt;wicket:'name' ..&gt;</code>. Please note that any such
	 *            tag must have been registered via
	 *            <code>WicketTagIdentifier.registerWellKnownTagName("name");</code>
	 * @param allowWicketComponentsInBodyMarkup
	 *            {@code false} for Panel and {@code true} for Border components. If Panel then the
	 *            body markup should only contain raw markup, which is ignored (removed), but no
	 *            Wicket Component. With Border components, the body markup will be associated with
	 *            the Body Component.
	 */
	public PanelMarkupSourcingStrategy(final String wicketTagName,
		final boolean allowWicketComponentsInBodyMarkup)
	{
		super(wicketTagName);

		this.allowWicketComponentsInBodyMarkup = allowWicketComponentsInBodyMarkup;
	}

	/**
	 * Constructor.
	 * 
	 * @param allowWicketComponentsInBodyMarkup
	 *            {@code false} for Panel and {@code true} for Border components. If Panel then the
	 *            body markup should only contain raw markup, which is ignored (removed), but no
	 *            Wicket Component. With Border components, the body markup will be associated with
	 *            the Body Component.
	 */
	public PanelMarkupSourcingStrategy(final boolean allowWicketComponentsInBodyMarkup)
	{
		this(Panel.PANEL, allowWicketComponentsInBodyMarkup);
	}

	/**
	 * Skip the panel's body markup which is expected to contain raw markup only (no wicket
	 * components) and which will be ignored / removed. It'll be replaced with the content of the
	 * associated markup file.
	 */
	@Override
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		if (allowWicketComponentsInBodyMarkup)
		{
			// Skip the body markup. Will be picked up by the Body component.
			markupStream.skipToMatchingCloseTag(openTag);
		}
		else
		{
			// Skip the components body. Like with Panels or Fragments, it'll be replaced with the
			// associated markup
			if (markupStream.getPreviousTag().isOpen())
			{
				markupStream.skipRawMarkup();
				if (markupStream.get().closes(openTag) == false)
				{
					StringBuilder msg = new StringBuilder();

					msg.append("Close tag not found for tag: ")
						.append(openTag.toString())
						.append(". For ")
						.append(component.getClass().getSimpleName())
						.append(" Components only raw markup is allow in between the tags but not ")
						.append("other Wicket Component. Component: ")
						.append(component.toString());

					throw new MarkupException(markupStream, msg.toString());
				}
			}
		}

		renderAssociatedMarkup(component);
	}
}
