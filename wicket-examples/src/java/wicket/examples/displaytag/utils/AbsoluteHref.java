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
package wicket.examples.displaytag.utils;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebRequestCycle;

/**
 * @author Juergen Donnerstag
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AbsoluteHref extends WebComponent
{
    /**
     * 
     * @param componentName
     */
    public AbsoluteHref(final String componentName)
    {
        super(componentName);
    }
    
    /**
     * @see wicket.Component#handleComponentTag(wicket.markup.ComponentTag)
     */
    protected void handleComponentTag(ComponentTag tag)
    {
        String href = tag.getString("href");
        if (href.charAt(0) != '/')
        {
            WebRequestCycle hcycle = (WebRequestCycle)getRequestCycle();
            String requestUrl = ((WebRequest)hcycle.getRequest()).getHttpServletRequest().getRequestURL().toString();
            String urlPrefix = hcycle.urlPrefix().toString();
            int idx = requestUrl.indexOf(urlPrefix);
            if (idx > 0)
            {
                urlPrefix = requestUrl.substring(0, idx + urlPrefix.length());
                href = urlPrefix + "/" + href;
            }
            tag.put("href", href);
        }
        
        super.handleComponentTag(tag);
    }

    /**
     * 
     * @see wicket.Component#handleComponentTagBody(wicket.markup.MarkupStream, wicket.markup.ComponentTag)
     */
    protected void handleComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
    {
    }
}
