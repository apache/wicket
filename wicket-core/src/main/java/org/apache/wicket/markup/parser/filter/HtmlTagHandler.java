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
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;

/**
 * Keeps the &lt;html&gt; tag of page markup as a tag, so the page can add attributes to it when
 * it renders, instead of merging it into the surrounding raw markup. Markup that chooses between
 * several &lt;html&gt; tags with conditional comments has each of them handled. The tag gets the
 * reserved id {@link #HTML_ID} and the {@link ComponentTag#RENDERED_BY_PAGE} flag; no component
 * is created for it. An &lt;html&gt; tag with a <code>wicket:id</code> is left to its component.
 *
 * @see org.apache.wicket.markup.html.WebPage#onHtmlTag(ComponentTag)
 * @since 12.0.0
 */
public final class HtmlTagHandler extends AbstractMarkupFilter
{
	/** The id assigned to the &lt;html&gt; tag of page markup */
	public static final String HTML_ID = "_html_";

	/**
	 * Construct.
	 *
	 * @param markupResourceStream
	 *            the resource stream of the markup being parsed
	 */
	public HtmlTagHandler(final MarkupResourceStream markupResourceStream)
	{
		super(markupResourceStream);
	}

	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		if (tag.isOpen() && (tag.getId() == null) && (tag.getNamespace() == null) &&
			"html".equalsIgnoreCase(tag.getName()))
		{
			tag.setId(HTML_ID);
			tag.setFlag(ComponentTag.RENDERED_BY_PAGE, true);
		}

		return tag;
	}
}
