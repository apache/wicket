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
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import wicket.Application;
import wicket.markup.ComponentTag;
import wicket.markup.Markup;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.util.value.IValueMap;

/**
 * This is a markup inline filter. It determines the Wicket namespace name from
 * the markup Examples are xmlns:wicket or
 * xmlns:wicket="http://wicket.sourceforge.net".
 * 
 * @see wicket.markup.MarkupParser
 * @author Juergen Donnerstag
 */
public final class WicketNamespaceHandler extends AbstractMarkupFilter
{
	/** Wicket URI */
	private static final String WICKET_URI = "http://wicket.sourceforge.net";

	/** The markup created by reading the markup file */
	private final Markup markup;

	/**
	 * namespace prefix: e.g. <html
	 * xmlns:wicket="http://wicket.sourceforge.net">
	 */
	private static final String XMLNS = "xmlns:";

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The markup created by reading the markup file
	 */
	public WicketNamespaceHandler(final Markup markup)
	{
		this.markup = markup;
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag from markup to be processed. If null, no more tags
	 *         are available
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the markup.
		// If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if (tag == null)
		{
			return tag;
		}

		if (tag.isOpen() && "html".equals(tag.getName().toLowerCase()))
		{
			final String namespace = determineWicketNamespace(tag);
			if (namespace != null)
			{
				markup.setWicketNamespace(namespace);
			}
		}

		return tag;
	}

	/**
	 * Determine wicket namespace from xmlns:wicket or
	 * xmlns:wicket="http://wicket.sourceforge.net"
	 * 
	 * @param tag
	 * @return Wicket namespace
	 */
	private String determineWicketNamespace(final ComponentTag tag)
	{
		// For all tags attributes
		final IValueMap attributes = tag.getAttributes();
		final Iterator it = attributes.entrySet().iterator();
		while (it.hasNext())
		{
			final Map.Entry entry = (Map.Entry)it.next();

			// Find attributes with namespace "xmlns"
			final String attributeName = (String)entry.getKey();
			if (attributeName.startsWith(XMLNS))
			{
				final String xmlnsUrl = (String)entry.getValue();

				// If Wicket relevant ...
				if ((xmlnsUrl == null) || (xmlnsUrl.trim().length() == 0)
						|| xmlnsUrl.startsWith(WICKET_URI))
				{
					// Set the Wicket namespace for wicket tags (e.g.
					// <eicket:panel>) and attributes (e.g. wicket:id)
					final String namespace = attributeName.substring(XMLNS.length());
					if (Application.get().getMarkupSettings().getStripWicketTags())
					{
						attributes.remove(attributeName);
						
						// Make sure the parser knows it has been changed
						tag.setModified(true);
					}
					
					return namespace;
				}
			}
		}

		return null;
	}
}
