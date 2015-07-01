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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.WicketParseException;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.AutoLabelTextResolver;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.FragmentResolver;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;
import org.apache.wicket.markup.resolver.MarkupInheritanceResolver;
import org.apache.wicket.markup.resolver.WicketContainerResolver;
import org.apache.wicket.markup.resolver.WicketMessageResolver;
import org.apache.wicket.util.string.Strings;


/**
 * This is a markup inline filter. It identifies xml tags which have a special meaning for Wicket.
 * There are two type of tags which have a special meaning for Wicket.
 * <p>
 * <ul>
 * <li>All tags with Wicket namespace, e.g. &lt;wicket:remove&gt;</li>
 * <li>All tags with an attribute like wicket:id="myLabel"</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public final class WicketTagIdentifier extends AbstractMarkupFilter
{
	/** List of well known wicket tag names */
	private static final Set<String> WELL_KNOWN_TAG_NAMES = new HashSet<>();

	static {
		WELL_KNOWN_TAG_NAMES.add(Border.BORDER);
		WELL_KNOWN_TAG_NAMES.add(Border.BODY);
		WELL_KNOWN_TAG_NAMES.add(AutoLabelTextResolver.LABEL);
		WELL_KNOWN_TAG_NAMES.add(Panel.PANEL);
		WELL_KNOWN_TAG_NAMES.add(EnclosureHandler.ENCLOSURE);
		WELL_KNOWN_TAG_NAMES.add(WicketLinkTagHandler.LINK);
		WELL_KNOWN_TAG_NAMES.add(WicketRemoveTagHandler.REMOVE);
		WELL_KNOWN_TAG_NAMES.add(FragmentResolver.FRAGMENT);
		WELL_KNOWN_TAG_NAMES.add(HtmlHeaderResolver.HEAD);
		WELL_KNOWN_TAG_NAMES.add(HtmlHeaderResolver.HEADER_ITEMS);
		WELL_KNOWN_TAG_NAMES.add(MarkupInheritanceResolver.CHILD);
		WELL_KNOWN_TAG_NAMES.add(MarkupInheritanceResolver.EXTEND);
		WELL_KNOWN_TAG_NAMES.add(WicketContainerResolver.CONTAINER);
		WELL_KNOWN_TAG_NAMES.add(WicketMessageResolver.MESSAGE);
	}

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The markup as known by now
	 */
	public WicketTagIdentifier(final MarkupResourceStream markup)
	{
		super(markup);
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for Wicket specific tags.
	 * <p>
	 * Note: The xml parser - the next MarkupFilter in the chain - returns XmlTags which are a
	 * subclass of MarkupElement. The implementation of this filter will return either ComponentTag
	 * or WicketTag. Both are subclasses of MarkupElement and both maintain a
	 * reference to the XmlTag. But no XmlTag is returned.
	 * 
	 * @see org.apache.wicket.markup.parser.IMarkupFilter#nextElement()
	 * @return The next tag from markup to be processed. If {@code null} then no more tags are available
	 */
	@Override
	protected MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		final String namespace = getWicketNamespace();

		// If the form <tag wicket:id = "value"> is used
		final String wicketIdValue = tag.getAttributes().getString(namespace + ":id");

		// Identify tags with Wicket namespace
		if (namespace.equalsIgnoreCase(tag.getNamespace()))
		{
			// It is <wicket:...>
			tag = new WicketTag(tag.getXmlTag());

			if (Strings.isEmpty(wicketIdValue))
			{
				// Make it a Wicket component. Otherwise it would be RawMarkup
				tag.setId(namespace + "_" + tag.getName() + getRequestUniqueId());
				tag.setModified(true);

				if (tag.isClose() == false)
				{
					tag.setAutoComponentTag(true);
				}
			}

			// If the tag is not a well-known wicket namespace tag
			if (!isWellKnown(tag))
			{
				// give up
				throw new WicketParseException("Unknown tag name with Wicket namespace: '" +
					tag.getName() + "'. Might be you haven't installed the appropriate resolver?",
					tag);
			}
		}

		if (wicketIdValue != null)
		{
			if (wicketIdValue.trim().length() == 0)
			{
				throw new WicketParseException(
					"The wicket:id attribute value must not be empty. May be unmatched quotes?!?",
					tag);
			}
			// Make it a wicket component. Otherwise it would be RawMarkup
			tag.setId(wicketIdValue);
		}

		return tag;
	}

	/**
	 * Register a new well known wicket tag name (e.g. panel)
	 * 
	 * @param name
	 */
	public static void registerWellKnownTagName(final String name)
	{
		String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
		WELL_KNOWN_TAG_NAMES.add(lowerCaseName);
	}

	/**
	 * 
	 * @param tag
	 * @return true, if name is known
	 */
	private boolean isWellKnown(final ComponentTag tag)
	{
		String lowerCaseTagName = tag.getName().toLowerCase(Locale.ENGLISH);
		return WELL_KNOWN_TAG_NAMES.contains(lowerCaseTagName);
	}
}
