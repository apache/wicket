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
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.parser.IMarkupFilter;


/**
 * Base class for markup filters
 * 
 * @see org.apache.wicket.markup.MarkupParser
 * 
 * @author Juergen Donnerstag
 */
public abstract class BaseMarkupFilter extends AbstractMarkupFilter
{
	/**
	 * Construct.
	 */
	public BaseMarkupFilter()
	{
		super();
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The next element in the chain.
	 */
	public BaseMarkupFilter(final IMarkupFilter parent)
	{
		super(parent);
	}

	/**
	 * @see org.apache.wicket.markup.parser.IMarkupFilter#nextTag()
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the next MarkupFilter in the chain.
		// If null, no more tags are available
		final MarkupElement tag = getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		return nextTag((ComponentTag)tag);
	}

	/**
	 * Invoked with the next ComponentTag
	 * 
	 * @param tag
	 * @return the next tag
	 * @throws ParseException
	 */
	protected abstract MarkupElement nextTag(ComponentTag tag) throws ParseException;
}
