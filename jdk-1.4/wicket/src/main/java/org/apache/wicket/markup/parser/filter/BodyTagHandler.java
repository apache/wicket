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
import org.apache.wicket.markup.html.internal.HtmlBodyContainer;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;


/**
 * Marks the body tag with a special id so that HtmlBodyContainer can easily
 * locate it and attach itself to it
 * 
 * @see org.apache.wicket.markup.MarkupParser
 * @author Juergen Donnerstag
 */
public final class BodyTagHandler extends AbstractMarkupFilter
{
	/**
	 * Construct.
	 */
	public BodyTagHandler()
	{
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * 
	 * @see org.apache.wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag from markup to be processed. If null, no more tags
	 *         are available
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the markup.
		// If null, no more tags are available
		ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null)
		{
			return tag;
		}

		// must be <body>
		if (tag.isOpen() && "body".equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
		{
			if (tag.getId() == null)
			{
				tag.setId(HtmlBodyContainer.BODY_ID);
				tag.setAutoComponentTag(true);
				tag.setModified(true);
			}
		}

		return tag;
	}
}
