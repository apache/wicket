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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.markup.parser.IMarkupFilter;

/**
 * This is a markup inline filter. It detects &t;body&gt; tags with onLoad
 * attribute. The onLoad attribute must be copied from the component's
 * markup to the Page's markup. In case onLoad loads and runs a javascript,
 * it must either be referenced by file or included in &lt;wicket:head&gt;. 
 * 
 * @author Juergen Donnerstag
 */
public final class BodyOnLoadHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static final Log log = LogFactory.getLog(BodyOnLoadHandler.class);
	
	/**
	 * Construct.
	 * 
	 * @param parent
	 *            The next MarkupFilter in the chain
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
		if (tag.isOpen() && "body".equalsIgnoreCase(tag.getName()))
		{
		    if (tag.getId() == null)
		    {
		        tag.setId("_body");
		    }
        }
		
		return tag;
	}
}
