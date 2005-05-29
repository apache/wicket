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
package wicket.markup.html.panel;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HeaderPart;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;

/**
 * A panel is a reusable component that holds markup and other components.
 * <p>
 * Whereas WebMarkupContainer is an inline container like
 * <pre>
 *  ...
 *  &lt;span wicket:id=&quot;xxx&quot;&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/span&gt;
 *  ...
 * </pre>
 * a Panel has its own associated markup file and the container content is
 * taken from that file, like:
 * <pre>
 *  &lt;span wicket:id=&quot;mypanel&quot;/&gt;
 * 
 *  TestPanel.html
 *  &lt;wicket:panel&gt;
 *    &lt;span wicket:id=&quot;mylabel&quot;&gt;My label&lt;/span&gt;
 *    ....
 *  &lt;/wicket:panel&gt;
 * </pre>
 * 
 * @author Jonathan Locke
 */
public class Panel extends WebMarkupContainer
{
    /**
     * @see wicket.Component#Component(String)
     */
    public Panel(final String id)
    {
        super(id);
    }
    
    /**
     * @see wicket.Component#Component(String, IModel)
     */
    public Panel(final String id, final IModel model)
    {
        super(id, model);
    }    

    /**
     * Renders this component.
     */
    protected final void onRender()
    {
        // Render the tag that included this html compoment
        final MarkupStream markupStream = findMarkupStream();

        // True if our panel is referenced by <wicket:panel>
        final boolean atOpenTag = markupStream.atOpenTag();

        // In order to be html compliant (though we are xhtml compliant already) 
        // and even more intuitive, we open up the tag, change it from open-close to
        // open, the panel now becomes the tag body and we'll close it manually
        // later.
        final ComponentTag openTag = markupStream.getTag().mutable();
        openTag.setType(XmlTag.OPEN);
        if (getRenderBodyOnly() == false)
        {
            renderComponentTag(openTag);
        }

        // Render the associated markup
        renderAssociatedMarkup("panel",
                "Markup for a panel component has to contain part '<wicket:panel>'");
        
        if (getRenderBodyOnly() == false)
        {
	        // Close the manually opened panel tag.
	        getResponse().write(openTag.syntheticCloseTagString());
        }
        
        // Skip opening tag
		markupStream.next();
		
        // If we are at an open tag, then there is nested preview markup
        if (atOpenTag)
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
	        
	        // Skip closing tag
			markupStream.next();
        }
    }

    /**
     * @see wicket.markup.html.WebMarkupContainer#getHeaderPart(int)
     */
    public HeaderPart getHeaderPart(int index)
    {
    	return new HeaderPart(this, index);
    }
}
