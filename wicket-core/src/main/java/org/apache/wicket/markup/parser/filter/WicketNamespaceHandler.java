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
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.util.value.IValueMap;


/**
 * This is a markup inline filter. It determines the Wicket namespace name from the markup. Examples
 * are xmlns:wicket or xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.3-strict.dtd"
 * though every URI that starts with "http://wicket.apache.org" will work as well.
 * 
 * @see org.apache.wicket.markup.MarkupParser
 * @author Juergen Donnerstag
 */
public final class WicketNamespaceHandler extends AbstractMarkupFilter
{
	/** Wicket URI */
	private static final String WICKET_URI = "http://wicket.apache.org";

	/**
	 * namespace prefix: e.g. <html xmlns:wicket="http://wicket.apache.org">
	 */
	private static final String XMLNS = "xmlns:";

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The markup created by reading the markup file
	 */
	public WicketNamespaceHandler(final MarkupResourceStream markup)
	{
		super(markup);
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		if (tag.isOpen() && "html".equals(tag.getName().toLowerCase(Locale.ROOT)))
		{
			final String namespace = determineWicketNamespace(tag);
			if (namespace != null)
			{
				getMarkupResourceStream().setWicketNamespace(namespace);
			}
		}

		return tag;
	}

	/**
	 * Determine wicket namespace from xmlns:wicket or xmlns:wicket="http://wicket.apache.org"
	 * 
	 * @param tag
	 * @return Wicket namespace
	 */
	private String determineWicketNamespace(final ComponentTag tag)
	{
		// For all tags attributes
		final IValueMap attributes = tag.getAttributes();
		for (Map.Entry<String, Object> entry : attributes.entrySet())
		{
			// Find attributes with namespace "xmlns"
			final String attributeName = entry.getKey();
			if (attributeName.startsWith(XMLNS))
			{
				final String xmlnsUrl = (String)entry.getValue();

				// If Wicket relevant ...
				if ((xmlnsUrl == null) || (xmlnsUrl.trim().length() == 0) ||
					xmlnsUrl.startsWith(WICKET_URI))
				{
					// Set the Wicket namespace for wicket tags (e.g.
					// <wicket:panel>) and attributes (e.g. wicket:id)
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
