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
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;


/**
 * This is a markup inline filter.
 * <p>
 * It assumes that {@link org.apache.wicket.markup.parser.filter.WicketTagIdentifier}
 * has been called first and search for a &lt;head&gt; tag (note: not wicket:head). Provided the markup contains a
 * &lt;body&gt; tag it will automatically prepend a &lt;head&gt; tag if missing.
 * </p>
 * <p>
 * Additionally this filter handles &lt;wicket:header-items/&gt;. If there is such tag then it is marked
 * as the one that should be used as {@link org.apache.wicket.markup.html.internal.HtmlHeaderContainer}, by
 * setting its id to {@value #HEADER_ID}.
 * </p>
 * <p>
 * Note: This handler is only relevant for Pages (see MarkupParser.newFilterChain())
 * 
 * @see org.apache.wicket.markup.MarkupParser
 * @see org.apache.wicket.markup.resolver.HtmlHeaderResolver
 * @author Juergen Donnerstag
 */
public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter
{
	public static final String BODY = "body";
	public static final String HEAD = "head";

	/** The automatically assigned wicket:id to &gt;head&lt; tag */
	public static final String HEADER_ID = "_header_";

	public static final String HEADER_ID_ITEM = "_header_item_";

	/** True if &lt;head&gt; has been found already */
	private boolean foundHead = false;

	/** True if &lt;/head&gt; has been found already */
	private boolean foundClosingHead = false;
	
	/** True if &lt;/wicket:header-items&gt; has been found already */
	private boolean foundHeaderItemsTag = false;

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
		super(markup.getMarkupResourceStream());
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
				handleHeadTag(tag);
			}
			else
			{
				// we found <wicket:head>
				foundHead = true;
				foundClosingHead = true;
			}
		}
		else if (HtmlHeaderResolver.HEADER_ITEMS.equalsIgnoreCase(tag.getName()) &&
				tag.getNamespace().equalsIgnoreCase(getWicketNamespace()))
		{
			handleHeaderItemsTag(tag);
		}
		else if (BODY.equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
		{
			handleBodyTag();
		}

		return tag;
	}

	/**
	 * Handle tag &lt;body&gt;
	 */
	private void handleBodyTag()
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
	}

	/**
	 * Handle tag &lt;wicket:header-items&gt;
	 * 
	 * @param tag
	 */
	private void handleHeaderItemsTag(ComponentTag tag)
	{
		if (foundHeaderItemsTag)
		{
			throw new MarkupException(new MarkupStream(markup),
					"More than one <wicket:header-items/> detected in the <head> element. Only one is allowed.");
		}
		else if (foundClosingHead)
		{
			throw new MarkupException(new MarkupStream(markup),
					"Detected <wicket:header-items/> after the closing </head> element.");
		}

		foundHeaderItemsTag = true;
		tag.setId(HEADER_ID);
		tag.setAutoComponentTag(true);
		tag.setModified(true);
	}

	/**
	 * Handle tag &lt;head&gt;
	 * @param tag
	 */
	private void handleHeadTag(ComponentTag tag)
	{
		// we found <head>
		if (tag.isOpen())
		{
			if(foundHead)
			{
				throw new MarkupException(new MarkupStream(markup),
					"Tag <head> is not allowed at this position (do you have multiple <head> tags in your markup?).");
			}
			
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
			if (foundHeaderItemsTag)
			{
				// revert the settings from above
				ComponentTag headOpenTag = tag.getOpenTag();
				// change the id because it is special. See HtmlHeaderResolver
				headOpenTag.setId(HEADER_ID + "-Ignored");
				headOpenTag.setAutoComponentTag(false);
				headOpenTag.setModified(false);
				headOpenTag.setFlag(ComponentTag.RENDER_RAW, true);
			}

			foundClosingHead = true;
		}
	}

	/**
	 * Insert &lt;head&gt; open and close tag (with empty body) to the current position.
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
