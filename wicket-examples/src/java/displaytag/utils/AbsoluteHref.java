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
package displaytag.utils;

import com.voicetribe.wicket.RequestCycle;
import com.voicetribe.wicket.markup.ComponentTag;
import com.voicetribe.wicket.markup.MarkupStream;
import com.voicetribe.wicket.markup.html.HtmlComponent;
import com.voicetribe.wicket.protocol.http.HttpRequest;
import com.voicetribe.wicket.protocol.http.HttpRequestCycle;

/**
 * @author Juergen Donnerstag
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AbsoluteHref extends HtmlComponent
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
     * 
     * @see com.voicetribe.wicket.Component#handleComponentTag(com.voicetribe.wicket.RequestCycle, com.voicetribe.wicket.markup.ComponentTag)
     */
    protected void handleComponentTag(RequestCycle cycle, ComponentTag tag)
    {
        String href = tag.getString("href");
        if (href.charAt(0) != '/')
        {
            HttpRequestCycle hcycle = (HttpRequestCycle)cycle;
            String requestUrl = ((HttpRequest)hcycle.getRequest()).getServletRequest().getRequestURL().toString();
            String urlPrefix = ((HttpRequestCycle)cycle).urlPrefix().toString();
            int idx = requestUrl.indexOf(urlPrefix);
            if (idx > 0)
            {
                urlPrefix = requestUrl.substring(0, idx + urlPrefix.length());
                href = urlPrefix + "/" + href;
            }
            tag.put("href", href);
        }
        
        super.handleComponentTag(cycle, tag);
    }

    /**
     * 
     * @see com.voicetribe.wicket.Component#handleBody(com.voicetribe.wicket.RequestCycle, com.voicetribe.wicket.markup.MarkupStream, com.voicetribe.wicket.markup.ComponentTag)
     */
    protected void handleBody(RequestCycle cycle,
            MarkupStream markupStream, ComponentTag openTag)
    {
        ; // nothting to do
    }
}
