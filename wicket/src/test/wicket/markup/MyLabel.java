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

import wicket.RequestCycle;
import wicket.markup.html.HtmlComponent;


/**
 * @author Juergen Donnerstag
 */
public class MyLabel extends HtmlComponent
{
    private String text = "";
    
    /**
     * Construct.
     * @param componentName
     */
    public MyLabel(final String componentName)
    {
        super(componentName);
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
     * @param cycle The request cycle
     * @param tag The tag to modify
     * @see wicket.Component#handleComponentTag(RequestCycle,
     *      wicket.markup.ComponentTag)
     */
    protected final void handleComponentTag(final RequestCycle cycle, final ComponentTag tag)
    {
        checkTag(tag, "component");
        super.handleComponentTag(cycle, tag);
    }

    /**
     * @see wicket.Component#handleBody(wicket.RequestCycle,
     *      wicket.markup.MarkupStream,
     *      wicket.markup.ComponentTag)
     */
    protected void handleBody(final RequestCycle cycle, final MarkupStream markupStream,
            final ComponentTag openTag)
    {
        replaceBody(cycle, markupStream, openTag, text);
    }
}
