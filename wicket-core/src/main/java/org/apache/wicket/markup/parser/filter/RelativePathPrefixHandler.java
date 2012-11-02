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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this filter is to make all "href", "src" and "background" attributes found in the
 * markup which contain a relative URL like "myDir/myPage.gif" actually resolve in the output HTML,
 * by prefixing them with with an appropriate path to make the link work properly, even if the
 * current page is being displayed at a mounted URL or whatever. It is applied to all non wicket
 * component tags, except for auto-linked tags.
 * 
 * It achieves this by being both an IMarkupFilter and IComponentResolver, and works similarly to
 * the &lt;wicket:message&gt; code. For each tag, we look to see if the path in "href", "src" and
 * "background" attributes is relative. If it is, we assume it's relative to the context path and we
 * should prefix it appropriately so that it resolves correctly for the current request, even if
 * that's for something that's not at the context root. This is done for ServletWebRequests by
 * prepending with "../" tokens, for example.
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
	 * The id automatically assigned to tags without an id which we need to prepend a relative path
	 * to.
	 */
	public static final String WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID = "_relative_path_prefix_";

	/** List of attribute names considered */
	private static final String attributeNames[] = new String[] { "href", "src", "background",
			"action" };

	/**
	 * Behavior that adds a prefix to src, href and background attributes to make them
	 * context-relative
	 */
	public static final Behavior RELATIVE_PATH_BEHAVIOR = new Behavior()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			// Modify all relevant attributes
			for (String attrName : attributeNames)
			{
				String attrValue = tag.getAttributes().getString(attrName);

				if ((attrValue != null) && (attrValue.startsWith("/") == false) &&
					(!attrValue.contains(":")) && !(attrValue.startsWith("#")))
				{
					tag.getAttributes().put(attrName,
						UrlUtils.rewriteToContextRelative(attrValue, RequestCycle.get()));
				}
			}
		}
	};

	/**
	 * Constructor for the IComponentResolver role.
	 */
	public RelativePathPrefixHandler()
	{
		this(null);
	}

	/**
	 * Constructor for the IMarkupFilter role
	 * @param markup
	 *      The markup created by reading the markup file
	 */
	public RelativePathPrefixHandler(final MarkupResourceStream markup)
	{
		super(markup);
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (tag.isClose())
		{
			return tag;
		}

		String wicketIdAttr = getWicketNamespace() + ":" + "id";

		// Don't touch any wicket:id component and any auto-components
		if ((tag instanceof WicketTag) || (tag.isAutolinkEnabled() == true) ||
			(tag.getAttributes().get(wicketIdAttr) != null))
		{
			return tag;
		}

		// Work out whether we have any attributes that require us to add a
		// behavior that prepends the relative path.
		for (String attrName : attributeNames)
		{
			String attrValue = tag.getAttributes().getString(attrName);
			if ((attrValue != null) && (attrValue.startsWith("/") == false) &&
				(!attrValue.contains(":")) && !(attrValue.startsWith("#")))
			{
				if (tag.getId() == null)
				{
					tag.setId(getWicketRelativePathPrefix());
					tag.setAutoComponentTag(true);
				}
				tag.addBehavior(RELATIVE_PATH_BEHAVIOR);
				tag.setModified(true);
				break;
			}
		}

		return tag;
	}

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		if ((tag != null) && (tag.getId().equals(getWicketRelativePathPrefix())))
		{
			String id = tag.getId() + container.getPage().getAutoIndex();

			// we do not want to mess with the hierarchy, so the container has to be
			// transparent as it may have wicket components inside. for example a raw anchor tag
			// that contains a label.
			return new TransparentWebMarkupContainer(id);
		}
		return null;
	}

	private String getWicketRelativePathPrefix()
	{
		return getWicketNamespace() + WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID;
	}
}
