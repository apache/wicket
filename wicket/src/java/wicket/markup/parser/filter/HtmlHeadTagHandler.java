/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.DynamicWicketTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.XmlTag;

/**
 * TODO
 * @author Eelco Hillenius
 */
public final class HtmlHeadTagHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static Log log = LogFactory.getLog(HtmlHeadTagHandler.class);

	private static final int NOTHING_FOUND_YET = 0;
	private static final int HTML_FOUND = 1;
	private static final int HEAD_FOUND = 2;
	private static final int INSERT_HEAD_OPEN = 3;
	private static final int INSERT_HEAD_CLOSE = 4;
	private static final int DONE = 99;

	private int position = NOTHING_FOUND_YET;

	private DynamicWicketTag openTag;
	private DynamicWicketTag closeTag;

	/**
	 * Construct.
	 */
	public HtmlHeadTagHandler()
	{
		super(null);
	}

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handles it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		if (position == INSERT_HEAD_OPEN)
		{
			XmlTag headOpenTag = new XmlTag();
			headOpenTag.setName("head");
			headOpenTag.setType(XmlTag.OPEN);
			openTag = new DynamicWicketTag(headOpenTag);
			openTag.setId("head");

			position = INSERT_HEAD_CLOSE;

			return openTag;
		}
		else if (position == INSERT_HEAD_CLOSE)
		{
			XmlTag headCloseTag = new XmlTag();
			headCloseTag.setName("head");
			headCloseTag.setType(XmlTag.CLOSE);
			closeTag = new DynamicWicketTag(headCloseTag);
			closeTag.setOpenTag(openTag);
			closeTag.setId("head");

			position = DONE;

			return closeTag;
		}

		// Get next tag. Null, if no more tag available
		final ComponentTag tag = (ComponentTag)getParent().nextTag();
		if (tag == null || position == DONE)
		{
			return tag;
		}

		final String tagName = tag.getName();

		if (position == NOTHING_FOUND_YET && "html".equals(tagName))
        {
			position = HTML_FOUND;
		}
		else if (position == HTML_FOUND)
		{
			// the html tag must be immediately followed by a head tag
			if (!"head".equals(tagName))
			{
				// must insert a header
				position = INSERT_HEAD_OPEN;				
			}
			else
			{
				// we're done with HEAD
				position = DONE;
			}
		}

		return tag;
	}
}
