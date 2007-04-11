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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.util.string.Strings;


/**
 * This is a markup inline filter. It identifies xml tags which have a special
 * meaning for Wicket. There are two type of tags which have a special meaning
 * for Wicket.
 * <p>
 * <ul>
 * <li>All tags with Wicket namespace, e.g. &lt;wicket:remove&gt;</li>
 * <li>All tags with an attribute like wicket:id="myLabel" </li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public final class WicketTagIdentifier extends AbstractMarkupFilter
{
	/** List of well known org.apache.wicket tag namses */
	private static List wellKnownTagNames;

	/** The current markup needed to get the markups namespace */
	private final Markup markup;

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The markup as known by now
	 */
	public WicketTagIdentifier(final Markup markup)
	{
		this.markup = markup;
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * <p>
	 * Note: The xml parser - the next MarkupFilter in the chain - returns
	 * XmlTags which are a subclass of MarkupElement. The implementation of this
	 * filter will return either ComponentTags or ComponentWicketTags. Both are
	 * subclasses of MarkupElement as well and both maintain a reference to the
	 * XmlTag. But no XmlTag is returned.
	 * 
	 * @see org.apache.wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag from markup to be processed. If null, no more tags
	 *         are available
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the markup.
		// If null, no more tags are available
		XmlTag xmlTag = (XmlTag)getParent().nextTag();
		if (xmlTag == null)
		{
			return xmlTag;
		}

		final String namespace = this.markup.getWicketNamespace();

		// convert tags of form <tag wicket:tag="bar"> to <wicket:bar>
		final String wicketTagAttr = namespace + ":tag";
		final String wicketTag = xmlTag.getAttributes().getString(wicketTagAttr);
		if (!Strings.isEmpty(wicketTag))
		{
			xmlTag = xmlTag.mutable();
			xmlTag.setNamespace(namespace);
			xmlTag.setName(wicketTag);
		}

		// Identify tags with Wicket namespace
		ComponentTag tag;
		if (namespace.equalsIgnoreCase(xmlTag.getNamespace()))
		{
			// It is <wicket:...>
			tag = new WicketTag(xmlTag);

			// Make it a Wicket component. Otherwise it would be RawMarkup
			tag.setId("_" + tag.getName());

			// If the tag is not a well-known wicket namespace tag
			if (!isWellKnown(xmlTag))
			{
				// give up
				throw new ParseException("Unknown tag name with Wicket namespace: '"
						+ xmlTag.getName()
						+ "'. Might be you haven't installed the appropriate resolver?", tag
						.getPos());
			}
		}
		else
		{
			// Everything else, except tags with Wicket namespace
			tag = new ComponentTag(xmlTag);
		}

		// If the form <tag wicket:id = "value"> is used
		final String value = tag.getAttributes().getString(namespace + ":id");
		if (value != null)
		{
			if (value.trim().length() == 0)
			{
				throw new ParseException(
						"The wicket:id attribute value must not be empty. May be unmatched quotes?!?",
						tag.getPos());
			}
			// Make it a wicket component. Otherwise it would be RawMarkup
			tag.setId(value);
		}

		return tag;
	}

	/**
	 * Register a new well known org.apache.wicket tag name (e.g. panel)
	 * 
	 * @param name
	 */
	public final static void registerWellKnownTagName(final String name)
	{
		if (wellKnownTagNames == null)
		{
			wellKnownTagNames = new ArrayList();
		}

		if (wellKnownTagNames.contains(name) == false)
		{
			wellKnownTagNames.add(name);
		}
	}

	private boolean isWellKnown(final XmlTag xmlTag)
	{
		final Iterator iterator = wellKnownTagNames.iterator();
		while (iterator.hasNext())
		{
			final String name = (String)iterator.next();
			if (xmlTag.getName().equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
}
