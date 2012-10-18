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
import org.apache.wicket.markup.WicketParseException;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;


/**
 * This is a markup inline filter. It identifies preview regions useful for HTML designers to design
 * the page. But they must be removed prior to sending the markup to the client. Preview regions are
 * enclosed by &lt;wicket:remove&gt; tags.
 * 
 * @author Juergen Donnerstag
 */
public final class WicketRemoveTagHandler extends AbstractMarkupFilter
{
	/** */
	public static final String REMOVE = "remove";

	static
	{
		// register "wicket:remove"
		WicketTagIdentifier.registerWellKnownTagName(REMOVE);
	}

	/**
	 * Construct.
	 */
	public WicketRemoveTagHandler()
	{
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		// If it is not a remove tag then we are finished
		if (!(tag instanceof WicketTag) || !((WicketTag)tag).isRemoveTag())
		{
			return tag;
		}

		// remove tag must not be open-close tags
		if (tag.isOpenClose())
		{
			throw new WicketParseException("Wicket remove tag must not be an open-close tag:", tag);
		}

		// fetch markup elements
		MarkupElement markupElement;
		while ((markupElement = getNextFilter().nextElement()) != null)
		{
			// skip non-component tags
			if((markupElement instanceof ComponentTag) == false)
			{
				continue;
			}

			// find the corresponding close tag for the given tag
			ComponentTag closeTag = (ComponentTag)markupElement;

			// No Wicket component tags are allowed within the preview region.
			// Wicket components will a component name assigned.
			if (closeTag.getId() == null)
			{
				continue;
			}

			// The first Wicket component following the preview region open
			// tag, must be it's corresponding close tag.
			if (closeTag.closes(tag))
			{
				// The tag (from open to close) should be ignored by
				// MarkupParser and not be added to the Markup.
				tag.setIgnore(true);
				return tag;
			}

			throw new WicketParseException(
				"Markup remove regions must not contain Wicket component tags:", closeTag);
		}

		throw new WicketParseException(
			"Did not find close tag for markup remove region. Open tag:", tag);
	}
}
