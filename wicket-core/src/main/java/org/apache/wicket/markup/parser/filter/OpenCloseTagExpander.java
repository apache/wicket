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
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.resolver.HtmlHeaderResolver;

/**
 * MarkupFilter that expands certain open-close tag as separate open and close tags. Firefox, unless
 * it gets text/xml mime type, treats these open-close tags as open tags which results in corrupted
 * DOM. This happens even with xhtml doctype.
 * 
 * In addition, some tags are required open-body-close for Wicket to work properly.
 * 
 * @author Juergen Donnerstag
 * @author Matej Knopp
 */
public class OpenCloseTagExpander extends AbstractMarkupFilter
{
	// A list of elements which should not be expanded from TagType.OPEN_CLOSE to TagType.OPEN + TagType.CLOSE
	// http://www.w3.org/TR/html-markup/syntax.html#void-element
	// area, base, br, col, command, embed, hr, img, input, keygen, link, meta, param, source, track, wbr

	static final List<String> REPLACE_FOR_TAGS = Arrays.asList("a", "q", "sub", "sup",
		"abbr", "acronym", "cite", "code", "del", "dfn", "em", "ins", "kbd", "samp", "var",
		"label", "textarea", "tr", "td", "th", "caption", "thead", "tbody", "tfoot", "dl", "dt",
		"dd", "li", "ol", "ul", "h1", "h2", "h3", "h4", "h5", "h6", "i",
		"pre",
		"title",
		"div",

		// tags from pre 1.5 days, shouldn't really be here but make this release more backwards
		// compatible
		"span", "p",
		"strong",
		"b",
		"e",
		"select",

		// @TODO by now an exclude list is probably shorter
		"article", "aside", "details", "summary", "figure", "figcaption", "footer",
		"header", "hgroup", "mark", "meter", "nav", "progress", "ruby", "rt", "rp", "section",
		"audio", "video", "canvas", "datalist", "output", HtmlHeaderResolver.HEADER_ITEMS);

	// temporary storage. Introduce into flow on next request
	private ComponentTag next = null;

	@Override
	public MarkupElement nextElement() throws ParseException
	{
		// Did we hold back an elem? Than return that first
		if (next != null)
		{
			MarkupElement rtn = next;
			next = null;
			return rtn;
		}

		return super.nextElement();
	}

	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		if (tag.isOpenClose())
		{
			String name = tag.getName();

			if (contains(name))
			{
				if (onFound(tag))
				{
					next = new ComponentTag(tag.getName(), TagType.CLOSE);
					next.setNamespace(tag.getNamespace());
					next.setOpenTag(tag);
					next.setModified(true);
				}
			}
		}

		return tag;
	}

	/**
	 * Can be subclassed to do other things. E.g. instead of changing it you may simply want to log
	 * a warning.
	 * 
	 * @param tag
	 * @return Must be true to automatically create and add a close tag.
	 */
	protected boolean onFound(final ComponentTag tag)
	{
		tag.setType(TagType.OPEN);
		tag.setModified(true);

		return true;
	}

	/**
	 * Allows subclasses to easily expand the list of tag which needs to be expanded.
	 * 
	 * @param name
	 * @return true, if needs expansion
	 */
	protected boolean contains(final String name)
	{
		return REPLACE_FOR_TAGS.contains(name.toLowerCase());
	}
}
