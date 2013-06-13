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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * A behavior that uses AbstractLink#getBeforeDisabledLink and AbstractLink#getAfterDisabledLink
 * to make Link disabled in the markup.
 *
 * @see org.apache.wicket.markup.html.link.AbstractLink#getDisablingBehavior()
 */
public class DisableLinkBehavior extends Behavior
{
	@Override
	public void beforeRender(Component component)
	{
		super.beforeRender(component);

		if (component instanceof AbstractLink)
		{
			AbstractLink link = (AbstractLink) component;

			// Draw anything before the body?
			if (!link.isLinkEnabled() && link.getBeforeDisabledLink() != null)
			{
				link.getResponse().write(link.getBeforeDisabledLink());
			}
		}
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);

		if (component instanceof AbstractLink)
		{
			AbstractLink link = (AbstractLink) component;

			if (link.isLinkEnabled() == false)
			{
				// if the tag is an anchor proper
				if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link") ||
						tag.getName().equalsIgnoreCase("area"))
				{
					// Change anchor link to span tag
					tag.setName("span");

					// Remove any href from the old link
					tag.remove("href");

					tag.remove("onclick");
				}
				// if the tag is a button or input
				else if ("button".equalsIgnoreCase(tag.getName()) ||
						"input".equalsIgnoreCase(tag.getName()))
				{
					tag.put("disabled", "disabled");
				}
			}
		}
	}

	@Override
	public void afterRender(Component component)
	{
		super.afterRender(component);

		if (component instanceof AbstractLink)
		{
			AbstractLink link = (AbstractLink) component;

			// Draw anything after the body?
			if (!link.isLinkEnabled() && link.getAfterDisabledLink() != null)
			{
				link.getResponse().write(link.getAfterDisabledLink());
			}
		}
	}
}
