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
package org.apache.wicket.markup.html.form;

import java.text.ParseException;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.util.string.Strings;

/**
 * Markup filter that identifies tags with the {@code wicket:for} attribute. See
 * {@link AutoLabelResolver} for details.
 * 
 * @author igor
 */
public class AutoLabelTagHandler extends AbstractMarkupFilter
{
	public AutoLabelTagHandler(MarkupResourceStream resourceStream)
	{
		super(resourceStream);
	}

	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		if (tag == null || tag.isClose() || tag instanceof WicketTag)
		{
			return tag;
		}

		String related = tag.getAttribute(getWicketNamespace() + AutoLabelResolver.WICKET_FOR);
		if (related == null)
		{
			return tag;
		}

		related = related.trim();
		if (Strings.isEmpty(related))
		{
			throw new ParseException("Tag contains an empty wicket:for attribute", tag.getPos());
		}

		if (!"label".equalsIgnoreCase(tag.getName()))
		{
			throw new ParseException("Attribute wicket:for can only be attached to <label> tag",
				tag.getPos());
		}

		if (tag.getId() != null)
		{
			throw new ParseException(
				"Attribute wicket:for cannot be used in conjunction with wicket:id", tag.getPos());
		}

		tag.setId(getClass().getName());
		tag.setModified(true);
		tag.setAutoComponentTag(true);
		return tag;
	}
}
