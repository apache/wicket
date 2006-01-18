/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup;

import wicket.markup.html.WebComponent;

/**
 * Dummy component used for ComponentCreateTagTest
 * 
 * @author Juergen Donnerstag
 */
public class MyLabel extends WebComponent
{
	private static final long serialVersionUID = 1L;
	
    private String text = "";
    
    /**
     * Construct.
     * @param id
     */
    public MyLabel(final String id)
    {
        super(id);
    }

    /**
     * Sets text.
     * @param text
     */
    public void setText(final String text)
    {
        this.text = text;
    }
    
    /**
     * Allows modification of component tag.
     * @param tag The tag to modify
     * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
     */
    protected final void onComponentTag(final ComponentTag tag)
    {
        checkComponentTag(tag, "component");
        super.onComponentTag(tag);
    }

    /**
     * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
        replaceComponentTagBody(markupStream, openTag, text);
    }
}
