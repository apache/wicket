/*
 * $Id: BodyOnLoadHandler.java 5385 2006-04-15 14:41:18 +0000 (Sat, 15 Apr 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-04-15 14:41:18 +0000 (Sat, 15 Apr
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.parser.filter;

import java.text.ParseException;

import wicket.Component;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;

/**
 * If you want to package and share ready-made components and if you want this
 * components to be easily pluggable than this component must have all the
 * relevant information. That includes javascript (inline or referenced file) as
 * well as CSS. With HTML these information have to be in the header of the
 * markup return to the browser. This is true for body onLoad attributes as
 * well. This markup inline filter detects &t;body&gt; tags with an 'onLoad'
 * attribute and which have <b>no</b> wicket:id attribute. The onLoad attribute
 * will be copied from the component's markup to the Page's markup to allow for
 * completely self-contained components. No changes to the Pages will be
 * necessary to use the component.
 * <p>
 * Note: The markup must contain a &lt;wicket:head&gt; tag which contains the
 * javascript and/or CSS to be copied to the Page's header.
 * <p>
 * Note: this handler is not relevant for Pages
 * 
 * @see wicket.markup.MarkupParser
 * @author Juergen Donnerstag
 */
public final class BodyOnLoadHandler extends AbstractMarkupFilter
{
	/** The automatically assigned wicket:id to &gt;body&lt; tag */
	public static final String BODY_ID = Component.AUTO_COMPONENT_PREFIX + "<body>";

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The parent of this component The next MarkupFilter in the
	 *            chain
	 */
	public BodyOnLoadHandler(final IMarkupFilter parent)
	{
		super(parent);
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
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

		// must be <body onload="...">
		if (tag.isOpen() && "body".equalsIgnoreCase(tag.getName()) && (tag.getNamespace() == null))
		{
			if (tag.getId() == null)
			{
				tag.setId(BODY_ID);
			}
		}

		return tag;
	}
}
