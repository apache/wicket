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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Response;
import wicket.markup.ComponentTag;

/**
 * Implementation of a special link component that can handle linkClicked events (implements
 * {@link wicket.markup.html.link.ILinkListener}) but that can be used with any kind of tag.
 * At render time, an onclick event handler is added to the tag (or the existing one
 * is replaced) like: <pre>onclick="location.href='/myapp/...'"</pre>.
 * <p>
 * The OnClickLink can be placed on any tag that has a onclick handler
 * (e.g. buttons, tr/ td's etc).
 * </p>
 * <p>
 * You can use a onClickLink like:
 * <pre>
 * add(new OnClickLink("myLink"){
 *
 *   public void linkClicked(RequestCycle cycle)
 *   {
 *      // do something here...  
 *   }
 * );
 * </pre>
 * and in your HTML file:
 * <pre>
 *  &lt;input type="button" id="wicket-remove" value="push me" /&gt;
 * </pre>
 * or (with a onclick handler that will be replaced but can be usefull when designing):
 * <pre>
 *  &lt;input type="button" onclick="alert('test');" id="wicket-remove" value="push me" /&gt;
 * </pre>
 * </p>
 *
 * @author Eelco Hillenius
 */
public abstract class OnClickLink extends AbstractLink
{
	/** Log. */
	private static Log log = LogFactory.getLog(OnClickLink.class);

    /**
     * @see wicket.Component#Component(String)
     */
    public OnClickLink(String componentName)
    {
        super(componentName);
    }

    /**
     * @see wicket.Component#Component(String, Serializable)
     */
    public OnClickLink(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * @see wicket.Component#Component(String, Serializable, String)
     */
    public OnClickLink(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
	 * Processes the component tag.
	 * @param tag Tag to modify
     * @see wicket.Component#onComponentTag(ComponentTag)
     */
    protected final void onComponentTag(final ComponentTag tag)
    {
        // Add simple javascript on click handler that links to this
        // link's linkClicked method
        final Response response = getRequestCycle().getResponse();
        final String url = getURL();
        // NOTE: don't encode to HTML as that is not valid JavaScript
        final PopupSettings popupSettings = getPopupSettings();
        if (popupSettings != null)
        {
        	popupSettings.setTarget("'" + url + "'");
            String popupScript = popupSettings.getPopupJavaScript();
            tag.put("onclick", popupScript);
        }
        else
        {
        	tag.put("onclick", "location.href='" + url + "';");
        }
    }
}