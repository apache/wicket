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
import wicket.markup.WicketHeaderTag;
import wicket.markup.WicketTag;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;
import wicket.markup.parser.XmlTag;

/**
 * This is a markup inline filter. It assumes that WicketTagIdentifier has been 
 * called first and that <wicket:head> tags have been detected. This filter checks
 * that <wicket:head> occur only within <head> sections.<p>
 * 
 * @author Juergen Donnerstag
 */
public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter
{
    /** Logging */
	private static final Log log = LogFactory.getLog(HtmlHeaderSectionHandler.class);

	/** Id of the header component */
	public static final String HEADER_ID = "_header";
	
    private static final String HEAD = "head";
    
    private static final int STATE_START = 0;
    private static final int STATE_HEAD_OPENED = 1;
    private static final int STATE_HEAD_CLOSED = 2;
    private static final int STATE_WICKET_HEAD_OPENED = 3;
    private static final int STATE_WICKET_HEAD_CLOSED = 4;
    private static final int STATE_BODY_FOUND = 5;
	
	/** current state */
	private int status;
	
	/** In case you want to add extra Components to the markup, just add them
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
		
		// Whatever there is left in the markup, <wicket:head> is no longer allowed
		if ((status == STATE_BODY_FOUND) || (status == STATE_HEAD_CLOSED))
		{
		    return tag;
		}

		// if it is <head> or <wicket:head>
		if (HEAD.equalsIgnoreCase(tag.getName()))
        {
		    // if <wicket:head>
			if (tag instanceof WicketTag)
			{
			    if (tag.isOpen() == true)
			    {
			        status = STATE_WICKET_HEAD_OPENED;
			    }
			    else
			    {
			        status = STATE_WICKET_HEAD_CLOSED;
			    }
			}
			else
			{
			    // it is <head>
			    if (tag.isOpen() == true)
			    {
			        status = STATE_HEAD_OPENED;
			    }
			    else
			    {
			        if (status != STATE_WICKET_HEAD_CLOSED)
			        {
			            // we found <head> but no <wicket:head>
			            insertWicketHeadTag(false);
			        }
			        status = STATE_HEAD_CLOSED;
			    }
			}
			
		    return tag;
        }
		
		if ("body".equalsIgnoreCase(tag.getName()))
        {
		    // <head> must always be before <body>
		    status = STATE_BODY_FOUND;
		    
		    // we found neither <head> nor <wicket:head>
		    insertWicketHeadTag(true);
		    return tag;
        }
		
		return tag;
	}
	
	/**
	 * Insert <wicket:head> open and close tag (with empty body)
	 * @param requiresHeadTag True, if no <head> was found
	 */
	private void insertWicketHeadTag(final boolean requiresHeadTag)
	{
		final XmlTag headOpenTag = new XmlTag();
		headOpenTag.setName(HEAD);
		headOpenTag.setType(XmlTag.OPEN);
		final WicketHeaderTag openTag = new WicketHeaderTag(headOpenTag);
		openTag.setId(HEADER_ID);
		openTag.setRequiresHtmlHeadTag(requiresHeadTag);
			
		final XmlTag headCloseTag = new XmlTag();
		headCloseTag.setName(HEAD);
		headCloseTag.setType(XmlTag.CLOSE);
		final WicketTag closeTag = new WicketTag(headCloseTag);
		closeTag.setOpenTag(openTag);
		closeTag.setId(HEADER_ID);

		// insert the tags into the markup stream
		tagList.add(openTag);
		tagList.add(closeTag);
		
		// done
		status = STATE_WICKET_HEAD_CLOSED;
	}
}
