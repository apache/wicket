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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.XmlTag.TagType;


/**
 * This is a markup inline filter. It assumes that WicketTagIdentifier has been called first and
 * search for a &lt;head&gt; tag (note: not wicket:head). Provided the markup contains a
 * &lt;body&gt; tag it will automatically prepend a &lt;head&gt; tag if missing.
 * <p>
 * Note: This handler is only relevant for Pages (see MarkupParser.newFilterChain())
 * 
 * @see org.apache.wicket.markup.MarkupParser
 * @author Juergen Donnerstag
 */
public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter
{
	private static final String BODY = "body";
	private static final String HEAD = "head";

	/** The automatically assigned wicket:id to &gt;head&lt; tag */
	public static final String HEADER_ID = "_header_";

	/** True if <head> has been found already */
	private boolean foundHead = false;

	/** True if </head> has been found already */
	private boolean foundClosingHead = false;

	/** True if all the rest of the markup file can be ignored */
	private boolean ignoreTheRest = false;

	/** The Markup available so far for the resource */
	private final Markup markup;

	/**
	 * Construct.
	 * 
	 * @param markup
	 *            The Markup object being filled while reading the markup resource
	 */
	public HtmlHeaderSectionHandler(final Markup markup)
	{
		this.markup = markup;
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		// Whatever there is left in the markup, ignore it
		if (ignoreTheRest == true)
		{
			return tag;
		}

		// if it is <head> or </head>
		if (HEAD.equalsIgnoreCase(tag.getName()))
		{
			if (tag.getNamespace() == null)
			{
				// we found <head>
				if (tag.isOpen())
				{
					foundHead = true;

					if (tag.getId() == null)
					{
						tag.setId(HEADER_ID);
						tag.setAutoComponentTag(true);
						tag.setModified(true);
					}
				}
				else if (tag.isClose())
				{
					foundClosingHead = true;
				}

				return tag;
			}
			else
			{
				// we found <wicket:head>
				foundHead = true;
				foundClosingHead = true;
			}
		}
		else if (BODY.equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
		{
			// WICKET-4511: We found <body> inside <head> tag. Markup is not valid!
			if (foundHead && !foundClosingHead)
			{
				throw new MarkupException(new MarkupStream(markup),
					"Invalid page markup. Tag <BODY> found inside <HEAD>");
			}

			// We found <body>
			if (foundHead == false)
			{
				insertHeadTag();
			}

			// <head> must always be before <body>
			ignoreTheRest = true;
			return tag;
		}

		return tag;
	}

	/**
	 * Insert <head> open and close tag (with empty body) to the current position.
	 */
	private void insertHeadTag()
	{
		// Note: only the open-tag must be a AutoComponentTag
		final ComponentTag openTag = new ComponentTag(HEAD, TagType.OPEN);
		openTag.setId(HEADER_ID);
		openTag.setAutoComponentTag(true);
		openTag.setModified(true);

		final ComponentTag closeTag = new ComponentTag(HEAD, TagType.CLOSE);
		closeTag.setOpenTag(openTag);
		closeTag.setModified(true);

		// insert the tags into the markup stream
		markup.addMarkupElement(openTag);
		markup.addMarkupElement(closeTag);
	}
}
