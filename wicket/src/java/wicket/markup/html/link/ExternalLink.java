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
package wicket.markup.html.link;

import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlContainer;


/**
 * Simple &lt;a href="..."&gt; pointing to any URL. Usually this is used
 * for links to destinations outside of Wicket.
 * 
 * @author Juergen Donnerstag
 */
public class ExternalLink extends HtmlContainer
{
    final private String href;
    final private String label;
    
    /**
     * Constructor.
     * 
     * @param componentName The name of this component
     * @param href the href attribute to set
     * @param label the label (body)
     */
    public ExternalLink(final String componentName, final String href, final String label)
    {
        super(componentName);
        
        this.href = href;
        this.label = label;
    }

    /**
     * @see wicket.Component#handleComponentTag(wicket.RequestCycle, wicket.markup.ComponentTag)
     */
    protected void handleComponentTag(RequestCycle cycle, ComponentTag tag)
    {
        if (href != null)
        {
            tag.put("href", href);
        }
    }

    /**
     * @see wicket.Component#handleBody(wicket.RequestCycle, wicket.markup.MarkupStream, wicket.markup.ComponentTag)
     */
    protected void handleBody(RequestCycle cycle, MarkupStream markupStream,
            ComponentTag openTag)
    {
        this.checkTag(openTag, "a");
        replaceBody(cycle, markupStream, openTag, label);
    }
}

///////////////////////////////// End of File /////////////////////////////////
