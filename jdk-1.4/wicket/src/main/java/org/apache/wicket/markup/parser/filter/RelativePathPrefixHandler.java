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
package org.apache.wicket.markup.parser.filter;

import java.text.ParseException;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this filter is to make all "href", "src" and "background" src
 * attributes found in the markup which contain a relative URL like
 * "myDir/myPage.gif" actually resolve in the output HTML, by prefixing them
 * with with an appropriate path to make the link work properly, even if the
 * current page is being displayed at a mounted URL or whatever. It is applied
 * to all non wicket component tags' attributes.
 * 
 * It achieves this by being both an IMarkupFilter and IComponentResolver, and
 * works similarly to the &lt;wicket:message&gt; code. For each tag, we look to
 * see if the path in "href", "src" and "background" attributes is relative. If
 * it is, we assume it's relative to the context path and we should prefix it
 * appropriately so that it resolves correctly for the current request, even if
 * that's for something that's not at the context root. This is done for
 * ServletWebRequests by prepending with "../" tokens, for example.
 * 
 * 
 * @author Al Maw
 */
public final class RelativePathPrefixHandler extends AbstractMarkupFilter
		implements
			IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(RelativePathPrefixHandler.class);

	/**
	 * The id automatically assigned to tags without an id which we need to
	 * prepend a relative path to.
	 */
	public static final String WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID = "-relative_path_prefix";

	/** List of attribute names considered */
	private static final String attributeNames[] = new String[] { "href", "src", "background" };

	/**
	 * Behavior that adds a prefix to src, href and background attributes to
	 * make them context-relative
	 */
	public static final IBehavior RELATIVE_PATH_BEHAVIOR = new AbstractBehavior()
	{
		private static final long serialVersionUID = 1L;

		public void onComponentTag(Component component, ComponentTag tag)
		{
			String prefix = null;

			// Modify all relevant attributes
			for (int i = 0; i < attributeNames.length; i++)
			{
				String attrName = attributeNames[i];
				String attrValue = tag.getAttributes().getString(attrName);

				if ((attrValue != null) && (attrValue.startsWith("/") == false) &&
						(attrValue.indexOf(":") < 0) && !(attrValue.startsWith("#")))
				{
					if (prefix == null)
					{
						prefix = RequestCycle.get().getRequest().getRelativePathPrefixToContextRoot();
					}
					attrValue = prefix + attrValue;
					tag.getAttributes().put(attrName, attrValue);
				}
			}
		}
	};

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handle it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see org.apache.wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag. If null, no more tags are available
		final ComponentTag tag = (ComponentTag)getParent().nextTag();
		if ((tag == null) || tag.isClose())
		{
			return tag;
		}

		// Don't touch any wicket:id component and any auto-components
		if ((tag.getId() != null) && (tag.getId().startsWith("-") == false))
		{
			return tag;
		}

		// Work out whether we have any attributes that require us to add a
		// behavior that prepends the relative path.
		for (int i = 0; i < attributeNames.length; i++)
		{
			String attrName = attributeNames[i];
			String attrValue = tag.getAttributes().getString(attrName);
			if ((attrValue != null) && (attrValue.startsWith("/") == false) &&
					(attrValue.indexOf(":") < 0) && !(attrValue.startsWith("#")))
			{
				tag.setId(WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID);
				tag.addBehavior(RELATIVE_PATH_BEHAVIOR);
				tag.setModified(true);
				break;
			}
		}

		return tag;
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.resolver.IComponentResolver#resolve(org.apache.wicket.MarkupContainer, org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		if (WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID.equals(tag.getId()))
		{
			final Component wc;
			String id = WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID +
					container.getPage().getAutoIndex();
			if (tag.isOpenClose())
			{
				wc = new WebComponent(id);
			}
			else
			{
				wc = new WebMarkupContainer(id);
			}
			container.autoAdd(wc, markupStream);
			return true;
		}
		return false;
	}
}
