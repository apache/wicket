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
package wicket.markup.html.image;

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.protocol.http.WebRequestCycle;

/**
 * Abstract base class for image components.
 *
 * @author Jonathan Locke
 */
public abstract class AbstractImage extends WebComponent
{
    /** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;
	
    /**
     * @see wicket.Component#Component(String)
     */
    public AbstractImage(final String name)
    {
        super(name);
    }

    /**
     * @see wicket.Component#Component(String, Serializable)
     */
    public AbstractImage(final String name, final Serializable object)
    {
        super(name, object);
    }

    /**
     * @see wicket.Component#Component(String, Serializable, String)
     */
    public AbstractImage(final String name, final Serializable object, final String expression)
    {
        super(name, object, expression);
    }

    /**
     * @see wicket.Component#onComponentTag(ComponentTag)
     */
    protected void onComponentTag(final ComponentTag tag)
    {
        checkComponentTag(tag, "img");
        super.onComponentTag(tag);
        final String url = ((WebRequestCycle)getRequestCycle()).urlFor(getResourcePath());
		tag.put("src", getResponse().encodeURL(url).replaceAll("&", "&amp;"));
    }
    
    /**
     * @return The path to the image resource that this component references
     */
    protected abstract String getResourcePath();
    
    /**
     * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
     */
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
    }
}
