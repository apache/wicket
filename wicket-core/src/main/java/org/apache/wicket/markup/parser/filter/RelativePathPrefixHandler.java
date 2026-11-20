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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.ComponentTag.IAutoComponentFactory;
import org.apache.wicket.markup.Markup;
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
 * The "srcset" attribute holds a comma-separated list of URLs, each optionally followed by a
 * descriptor like "480w" or "2x". Each relative URL in it is prefixed the same way, and the
 * descriptors are kept as they are.
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
			"action", "srcset" };

	private static final String SRCSET = "srcset";

	/**
	 * Behavior that adds a prefix to src, href, background and srcset attributes to make them
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

				if (SRCSET.equals(attrName))
				{
					if (attrValue != null)
					{
						tag.getAttributes().put(attrName, rewriteSrcset(attrValue));
					}
				}
				else if (isRelative(attrValue))
				{
					tag.getAttributes().put(attrName,
						UrlUtils.rewriteToContextRelative(attrValue, RequestCycle.get()));
				}
			}
		}
	};
	
	private static final IAutoComponentFactory FACTORY = new IAutoComponentFactory()
	{
		@Override
		public Component newComponent(MarkupContainer container, ComponentTag tag)
		{
			return new TransparentWebMarkupContainer(tag.getId());
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
	 * 
	 * @param markup
	 *            The markup created by reading the markup file
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

		// Don't touch any wicket:id component and any auto components
		if ((tag instanceof WicketTag) || (tag.isAutolinkEnabled() == true)
			|| (tag.getAttributes().get(wicketIdAttr) != null))
		{
			return tag;
		}
		
		// Work out whether we have any attributes that require us to add a
		// behavior that prepends the relative path.
		for (String attrName : attributeNames)
		{
			String attrValue = tag.getAttributes().getString(attrName);
			if (needsPrefix(attrName, attrValue))
			{
				if (tag.getId() == null)
				{
					tag.setId(getWicketRelativePathPrefix(null)
						+ getRequestUniqueId());
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
		if ((tag != null) && (tag.getId().startsWith(getWicketRelativePathPrefix(markupStream))))
		{
			// we do not want to mess with the hierarchy, so the container has to be
			// transparent as it may have wicket components inside. for example a raw anchor tag
			// that contains a label.
			return new TransparentWebMarkupContainer(tag.getId());
		}
		return null;
	}
	
	@Override
	public void postProcess(Markup markup)
	{
		/**
		 * https://issues.apache.org/jira/browse/WICKET-5724
		 * 
		 * Transparent component inside page body must allow queued children components.
		 */
		Iterator<MarkupElement> markupIterator = markup.iterator();
		while (markupIterator.hasNext())
		{
			MarkupElement next = markupIterator.next();

			if (next instanceof ComponentTag)
			{
				ComponentTag componentTag = (ComponentTag)next;

				/**
				 * if component tag is for a transparent component and contains "wicket:id", must be
				 * queueable.
				 */
				if (componentTag.containsWicketId()
					&& componentTag.getId().startsWith(getWicketRelativePathPrefix(null)))
				{
					componentTag.setAutoComponentFactory(FACTORY);
				}
			}
		}
	}
	
	private String getWicketRelativePathPrefix(final MarkupStream markupStream)
	{
		return getWicketNamespace(markupStream) + WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID;
	}

	private static boolean isRelative(final String url)
	{
		return (url != null) && !url.startsWith("/") && !url.contains(":") && !url.startsWith("#");
	}

	private static boolean needsPrefix(final String attrName, final String attrValue)
	{
		if (SRCSET.equals(attrName))
		{
			return (attrValue != null) && findSrcsetUrls(attrValue).stream()
				.anyMatch(bounds -> isRelative(attrValue.substring(bounds[0], bounds[1])));
		}
		return isRelative(attrValue);
	}

	private static String rewriteSrcset(final String srcset)
	{
		StringBuilder result = new StringBuilder(srcset.length() + 16);
		int copied = 0;
		for (int[] bounds : findSrcsetUrls(srcset))
		{
			String url = srcset.substring(bounds[0], bounds[1]);
			result.append(srcset, copied, bounds[0]);
			result.append(isRelative(url) ? UrlUtils.rewriteToContextRelative(url,
				RequestCycle.get()) : url);
			copied = bounds[1];
		}
		result.append(srcset, copied, srcset.length());
		return result.toString();
	}

	/**
	 * Finds the URLs of the image candidates in a srcset value, following the parsing rules of the
	 * HTML specification: a URL is a run of non-whitespace characters, trailing commas end the
	 * candidate, and otherwise its descriptors run up to the next comma.
	 *
	 * @param srcset
	 *            the srcset attribute value
	 * @return the start (inclusive) and end (exclusive) index of each URL
	 */
	private static List<int[]> findSrcsetUrls(final String srcset)
	{
		List<int[]> urls = new ArrayList<>();
		int length = srcset.length();
		int position = 0;
		while (position < length)
		{
			char c = srcset.charAt(position);
			if (Character.isWhitespace(c) || (c == ','))
			{
				position++;
				continue;
			}

			int urlEnd = position;
			while ((urlEnd < length) && !Character.isWhitespace(srcset.charAt(urlEnd)))
			{
				urlEnd++;
			}
			int trimmedEnd = urlEnd;
			while (srcset.charAt(trimmedEnd - 1) == ',')
			{
				trimmedEnd--;
			}
			urls.add(new int[] { position, trimmedEnd });

			if (trimmedEnd < urlEnd)
			{
				position = urlEnd;
			}
			else
			{
				int nextComma = srcset.indexOf(',', urlEnd);
				position = (nextComma == -1) ? length : nextComma + 1;
			}
		}
		return urls;
	}
}
