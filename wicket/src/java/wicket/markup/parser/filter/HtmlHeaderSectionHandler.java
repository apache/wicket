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
import wicket.markup.MarkupException;
import wicket.markup.WicketTag;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It assumes that WicketTagIdentifier has been 
 * called first and that <wicket:head> tags have been detected. This filter checks
 * that <wicket:head> occur only within <head> sections.<p>
 * 
 * Note: it is currently commented out, but it may also add <head> components
 * in case they are not found in the markup file.
 * 
 * @author Juergen Donnerstag
 */
public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static final Log log = LogFactory.getLog(HtmlHeaderSectionHandler.class);

	/** If true, we have seen <head> but not </head> yet. */
	private boolean withinHead; 
	
	/**
	 * Only if false, it will watch for head tags.
	 */
	private boolean done;
	
	/** In case you want to add extra Component to the markup, just add them
	 * to the list. MarkupParser will handle it.
	 */
	private List tagList;
	
	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The next MarkupFilter in the chain
	 */
	public HtmlHeaderSectionHandler(final IMarkupFilter parent)
	{
		super(parent);
	}
	
	/**
	 * In order to add ComponentTags which are NOT from markup file.
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
	 * Note: The xml parser - the next MarkupFilter in the chain - returns
	 * XmlTags which are a subclass of MarkupElement. The implementation of this
	 * filter will return either ComponentTags or ComponentWicketTags. Both are
	 * subclasses of MarkupElement as well and both maintain a reference to the
	 * XmlTag. But no XmlTag is returned.
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
		
		if (done == true)
		{
		    return tag;
		}

		if (!(tag instanceof WicketTag) && "head".equalsIgnoreCase(tag.getName()))
        {
		    withinHead = !withinHead;
		    if (tag.isClose() == true)
		    {
		        done = true;
		    }
		    
		    return tag;
        }
		
		if ("body".equalsIgnoreCase(tag.getName()))
        {
		    done = true;

			// TODO remove comment, to activate
/*		    
			final XmlTag headOpenTag = new XmlTag();
			headOpenTag.setName("head");
			headOpenTag.setType(XmlTag.OPEN);
			final ComponentTag openTag = new ComponentTag(headOpenTag);
			openTag.setId("_header");
				
			final XmlTag headCloseTag = new XmlTag();
			headCloseTag.setName("head");
			headCloseTag.setType(XmlTag.CLOSE);
			final ComponentTag closeTag = new ComponentTag(headCloseTag);
			closeTag.setOpenTag(openTag);
			closeTag.setId("_header");

			tagList.add(openTag);
			tagList.add(closeTag);
*/		
		    return tag;
        }
		
		if (!(tag instanceof WicketTag))
		{
		    return tag;
		}

		final WicketTag wtag = (WicketTag) tag;
		if ((withinHead == false) && (wtag.isHeadTag() == true))
		{
		    throw new MarkupException("<wicket:head> is only allow within <head> section.");
		}
		
		return tag;
	}
}
