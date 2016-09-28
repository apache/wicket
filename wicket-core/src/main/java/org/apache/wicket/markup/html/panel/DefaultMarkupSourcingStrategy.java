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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a no-op sourcing strategy implementing the default behavior for most components.
 * 
 * @author Juergen Donnerstag
 */
public final class DefaultMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy
{
	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(DefaultMarkupSourcingStrategy.class);

	private static final DefaultMarkupSourcingStrategy instance = new DefaultMarkupSourcingStrategy();

	/**
	 * 
	 * @return A singleton of the strategy
	 */
	public static DefaultMarkupSourcingStrategy get()
	{
		return instance;
	}

	/**
	 * Construct. Please use {@link #get()} instead.
	 */
	private DefaultMarkupSourcingStrategy()
	{
	}

	/**
	 * Nothing to add to the response by default
	 */
	@Override
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
	}

	/**
	 * Invoke the component's onComponentTagBody().
	 */
	@Override
	public void onComponentTagBody(final Component component, final MarkupStream markupStream,
		final ComponentTag openTag)
	{
		component.onComponentTagBody(markupStream, openTag);
	}

	/**
	 * Get the markup for the child component, which is assumed to be a child of 'container'.
	 */
	@Override
	public IMarkupFragment getMarkup(final MarkupContainer container, final Component child)
	{
		// If the sourcing strategy did not provide one, than ask the component.
		// Get the markup for the container
		IMarkupFragment containerMarkup = container.getMarkup();
		if (containerMarkup == null)
		{
			return null;
		}

		if (child == null)
		{
			return containerMarkup;
		}
		
		// Find the child's markup
		IMarkupFragment childMarkup = containerMarkup.find(child.getId());
		if (childMarkup != null)
		{
			return childMarkup;
		}
		
		return searchMarkupInTransparentResolvers(container, containerMarkup, child);
	}
}
