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

import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.parser.IMarkupFilter;
import org.apache.wicket.markup.parser.IXmlPullParser;
import org.apache.wicket.markup.parser.IXmlPullParser.ELEMENT_TYPE;


/**
 * 
 * @author Juergen Donnerstag
 */
public final class RootMarkupFilter implements IMarkupFilter
{
	/**  */
	private final IXmlPullParser parser;

	/**
	 * Construct.
	 * 
	 * @param parser
	 */
	public RootMarkupFilter(final IXmlPullParser parser)
	{
		this.parser = parser;
	}

	public final MarkupElement nextTag() throws ParseException
	{
		ELEMENT_TYPE type;
		while ((type = next()) != ELEMENT_TYPE.TAG)
		{
			if (type == ELEMENT_TYPE.NOT_INITIALIZED)
			{
				return null;
			}
		}

		return parser.getElement();
	}

	public IMarkupFilter getNextFilter()
	{
		return null;
	}

	public void setNextFilter(final IMarkupFilter parent)
	{
		throw new IllegalArgumentException("You can not set the parent with RootMarkupFilter.");
	}

	/**
	 * 
	 * @return The next XML element
	 * @throws ParseException
	 */
	private ELEMENT_TYPE next() throws ParseException
	{
		return parser.next();
	}
}
