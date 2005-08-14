/*
 * $Id: WicketTagIdentifier.java,v 1.4 2005/02/04 07:22:53 jdonnerstag
 * Exp $ $Revision$ $Date$
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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.WicketTag;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It assumes that WicketTagIdentifier has been
 * called first. It identifies &lt;wicket:extend&gt; tag which are used for
 * markup inheritance and remove everything (stops processing) after the tag has
 * been closed. Prior to the open tag only raw markup and &gt;head&lt; is
 * allowed.
 * 
 * @author Juergen Donnerstag
 */
public final class WicketExtendHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static final Log log = LogFactory.getLog(WicketExtendHandler.class);

	/**
	 * In case you want to add extra Components to the markup, just add them to
	 * the list. MarkupParser will handle it.
	 */
	private List tagList;

	/** True, if </wicket:extend> has been seen */
	private boolean ignoreRemainingTag = false;

	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The next MarkupFilter in the chain
	 */
	public WicketExtendHandler(final IMarkupFilter parent)
	{
		super(parent);
	}

	/**
	 * In order to add ComponentTags which are NOT from markup file.
	 * 
	 * @param tagList
	 */
	public void setTagList(final List tagList)
	{
		this.tagList = tagList;
	}

	/**
	 * Get the next tag from the next MarkupFilter in the chain and search for
	 * Wicket specific tags.
	 * <p>
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

		if (ignoreRemainingTag == true)
		{
			// ignore everything following </wicket:extend>
			return null;
		}

		if (tag instanceof WicketTag)
		{
			final WicketTag wtag = (WicketTag)tag;
			if (wtag.isExtendTag())
			{
				if (wtag.isClose())
				{
					ignoreRemainingTag = true;
				}
				else
				{
					// Note: This is a very special "command". It removes all
					// existing tags already in the tag list, thus removing 
				    // everything before <wicket:extend> AND it will remove 
				    // everything following </wicket:extend>
					tagList.add(null);
				}
			}
		}

		return tag;
	}
}
