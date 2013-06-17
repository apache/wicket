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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * A behavior to change the representation fo disabled links.
 * <p>
 * Markup tags {@code <a>}, {@code <link>} and {@code <area>} are replaced with a {@code <span>}.
 */
public final class DisabledLinkBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Simple insertion string to allow disabled links to look like <i>Disabled link </i>.
	 */
	private String beforeDisabledLink;

	/**
	 * Simple insertion string to allow disabled links to look like <i>Disabled link </i>.
	 */
	private String afterDisabledLink;

	/**
	 * Enclose each disabled link in {@code <em></em>}.
	 */
	public DisabledLinkBehavior()
	{
		this("<em>", "</em>");
	}

	/**
	 * Enclose each disabled link in the given markup.
	 * 
	 * @param beforeDisabledLink
	 *            markup to write before the link
	 * @param afterDisabledLink
	 *            markup to write after the link
	 */
	public DisabledLinkBehavior(String beforeDisabledLink, String afterDisabledLink)
	{
		this.beforeDisabledLink = beforeDisabledLink;
		this.afterDisabledLink = afterDisabledLink;
	}

	/**
	 * Sets the insertion string to allow disabled links to look like <i>Disabled link </i>.
	 * 
	 * @param afterDisabledLink
	 *            The insertion string
	 * @return this
	 */
	public DisabledLinkBehavior setAfterDisabledLink(final String afterDisabledLink)
	{
		if (afterDisabledLink == null)
		{
			throw new IllegalArgumentException(
				"Value cannot be null.  For no text, specify an empty String instead.");
		}
		this.afterDisabledLink = afterDisabledLink;
		return this;
	}

	/**
	 * Gets the insertion string to allow disabled links to look like <i>Disabled link </i>.
	 * 
	 * @return The insertion string
	 */
	public String getAfterDisabledLink()
	{
		return afterDisabledLink;
	}

	/**
	 * Sets the insertion string to allow disabled links to look like <i>Disabled link </i>.
	 * 
	 * @param beforeDisabledLink
	 *            The insertion string
	 * @return this
	 */
	public DisabledLinkBehavior setBeforeDisabledLink(final String beforeDisabledLink)
	{
		if (beforeDisabledLink == null)
		{
			throw new IllegalArgumentException(
				"Value cannot be null.  For no text, specify an empty String instead.");
		}
		this.beforeDisabledLink = beforeDisabledLink;
		return this;
	}

	/**
	 * Gets the insertion string to allow disabled links to look like <i>Disabled link </i>.
	 * 
	 * @return The insertion string
	 */
	public String getBeforeDisabledLink()
	{
		return beforeDisabledLink;
	}

	@Override
	public void beforeRender(Component component)
	{
		// Draw anything before the body?
		if (!component.isEnabledInHierarchy() && getBeforeDisabledLink() != null)
		{
			component.getResponse().write(getBeforeDisabledLink());
		}
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		if (!component.isEnabledInHierarchy())
		{
			// if the tag is an anchor proper
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link") ||
				tag.getName().equalsIgnoreCase("area"))
			{
				// Change anchor link to span tag
				tag.setName("span");
			}
		}
	}

	@Override
	public void afterRender(Component component)
	{
		// Draw anything after the body?
		if (!component.isEnabledInHierarchy() && getAfterDisabledLink() != null)
		{
			component.getResponse().write(getAfterDisabledLink());
		}
	}

	/**
	 * A listener to instantiations of {@link AbstractLink} to restores the disabled representation
	 * to that before Wicket 7.x.
	 */
	public static class LinkInstantiationListener implements IComponentInstantiationListener
	{
		/**
		 * Adds an {@link DisabledLinkBehavior} to all {@link AbstractLink}s.
		 */
		@Override
		public void onInstantiation(Component component)
		{
			if (component instanceof AbstractLink)
			{
				component.add(new DisabledLinkBehavior());
			}
		}
	}
}