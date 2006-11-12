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
package wicket.markup.parser.filter;

import java.text.ParseException;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;

/**
 * This is a markup inline filter. It identifies preview regions useful for HTML
 * designers to design the page. But they must be removed prior to sending the
 * markup to the client. Preview regions are enclosed by &lt;wicket:remove&gt;
 * tags.
 * 
 * @author Juergen Donnerstag
 */
public final class WicketRemoveTagHandler extends AbstractMarkupFilter
{
	/** Flag value to use as component name for ignored components */
	public static final String IGNORE = "<<Removed by WicketRemoveTagHandler>>";

	static
	{
		// register "wicket:remove"
		WicketTagIdentifier.registerWellKnownTagName("remove");
	}

	/**
	 * Construct.
	 * 
	 */
	public WicketRemoveTagHandler()
	{
	}

	/**
	 * Removes preview regions enclosed by &lt;wicket:remove&gt; tags. Note that
	 * for obvious reasons, nested components are not allowed.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return The next tag to be processed. Null, if not more tags are
	 *         available
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the next MarkupFilter in the chain
		// If null, no more tags are available
		final ComponentTag openTag = nextComponentTag();
		if (openTag == null)
		{
			return openTag;
		}

		// If it is not a remove tag, than we are finished
		if (!openTag.isRemoveTag())
		{
			return openTag;
		}

		// remove tag must not be open-close tags
		if (openTag.isOpenClose())
		{
			throw new ParseException("Wicket remove tag must not be an open-close tag: "
					+ openTag.toUserDebugString(), openTag.getPos());
		}

		// Find the corresponding close tag and remove all tags in between
		ComponentTag closeTag;
		while (null != (closeTag = (ComponentTag)getParent().nextTag()))
		{
			// No Wicket component tags are allowed within the preview region.
			// Wicket components will a component name assigned.
			if (closeTag.getId() == null)
			{
				continue;
			}

			// The first Wicket component following the preview region open
			// tag, must be it's corresponding close tag.
			if (closeTag.closes(openTag))
			{
				// Component's named with the IGNORE component name will be
				// ignored
				// by MarkupParser and not added to the Markup.
				openTag.setId(IGNORE);
				return openTag;
			}

			throw new ParseException(
					"Markup remove regions must not contain Wicket component tags. " + "tag: "
							+ closeTag.toUserDebugString(), closeTag.getPos());
		}

		throw new ParseException("Did not find close tag for markup remove region. " + "Open tag: "
				+ openTag.toUserDebugString(), openTag.getPos());
	}
}
