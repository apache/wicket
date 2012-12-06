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
import org.apache.wicket.markup.HtmlSpecialTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.IXmlPullParser;
import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;


/**
 * This is the root of all filters, which retrieves the next xml element from the xml parser.
 * 
 * @author Juergen Donnerstag
 */
public final class RootMarkupFilter extends AbstractMarkupFilter
{
	/** The xml parser */
	private final IXmlPullParser parser;

	/**
	 * Constructor.
	 *
	 * @param parser
	 *      the parser that reads the markup
	 * @deprecated Use #RootMarkupFilter(IXmlPullParser, MarkupResourceStream) instead
	 */
	public RootMarkupFilter(final IXmlPullParser parser)
	{
		this(parser, null);
	}

	/**
	 * Construct.
	 * 
	 * @param parser
	 */
	public RootMarkupFilter(final IXmlPullParser parser, MarkupResourceStream resourceStream)
	{
		super(resourceStream);
		this.parser = parser;
	}

	/**
	 * Skip all xml elements until the next tag.
	 */
	@Override
	public final MarkupElement nextElement() throws ParseException
	{
		HttpTagType type;
		while ((type = parser.next()) != HttpTagType.NOT_INITIALIZED)
		{
			if (type == HttpTagType.BODY)
			{
				continue;
			}
			else if (type == HttpTagType.TAG)
			{
				return new ComponentTag(parser.getElement());
			}
			else
			{
				return new HtmlSpecialTag(parser.getElement(), type);
			}
		}

		return null;
	}

	/**
	 * @return null. This is the root filter.
	 */
	@Override
	public final IMarkupFilter getNextFilter()
	{
		return null;
	}

	/**
	 * This is the root filter. Operation not allowed. An exception will be thrown.
	 */
	@Override
	public final void setNextFilter(final IMarkupFilter parent)
	{
		throw new IllegalArgumentException("You can not set the parent with RootMarkupFilter.");
	}

	/**
	 * Noop
	 */
	@Override
	protected MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		return tag;
	}

	/**
	 * Noop
	 */
	@Override
	public final void postProcess(Markup markup)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return parser.toString();
	}
}
