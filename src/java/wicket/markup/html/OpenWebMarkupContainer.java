/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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
package wicket.markup.html;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;

/**
 * Some components, e.g. Panel, do not require open-body-close tags. They instead
 * modify the open-close tag to be open-body-close.<p>
 * In case it is already open-body-close the body will be removed (preview region) and
 * hence must not contain any wicket tag.<p>
 * Component which make use of the base class should override onComponentTagBody
 * and must invoke the super implementation.
 * 
 * @author Juergen Donnerstag
 */
public class OpenWebMarkupContainer extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private transient boolean wasOpenCloseTag = false;
	
	/**
     * @see wicket.Component#Component(String)
     */
    public OpenWebMarkupContainer(final String id)
    {
        super(id);
    }
    
    /**
     * @see wicket.Component#Component(String, IModel)
     */
    public OpenWebMarkupContainer(final String id, final IModel model)
    {
        super(id, model);
    }    

    /**
     * 
     * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
     */
    protected void onComponentTag(final ComponentTag tag)
    {
    	if (tag.isOpenClose())
    	{
    		this.wasOpenCloseTag = true;
    		
    		// Convert <span wicket:id="myPanel" /> into 
    		// <span wicket:id="myPanel">...</span>  
    		tag.setType(XmlTag.OPEN);
    	}
    	super.onComponentTag(tag);
    }

    /**
     * 
     * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream, wicket.markup.ComponentTag)
     */
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
        // If we are at an open tag, then there is nested preview markup
        if (this.wasOpenCloseTag == false)
        {
			// Skip any raw markup in the body
			markupStream.skipRawMarkup();
			
			// Open tag must have close tag
			if (!markupStream.atCloseTag())
			{
				// There must be a component in this discarded body
				markupStream
						.throwMarkupException("Expected close tag.  Possible attempt to embed component(s) "
								+ "in the body of a component which discards its body");
			}
        }
    }
}
