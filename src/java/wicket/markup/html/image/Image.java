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
import java.util.Locale;

import wicket.IResourceListener;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.util.lang.Classes;
import wicket.util.resource.IResource;
import wicket.util.resource.Resource;
import wicket.util.string.Strings;

/**
 * An image component represents a localizable image resource. The image name
 * comes from the src attribute of the image tag that the component is attached
 * to.  The image component responds to requests made via IResourceListener's
 * resourceRequested method.  The image or subclass responds by returning an
 * IResource from getImageResource(String), where String is the source attribute
 * of the image tag.
 *
 * @author Jonathan Locke
 */
public class Image extends AbstractImage implements IResourceListener
{
    /** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	/**
	 * The string representation of the resource to be loaded. Actual
	 * resource is not loaded until the image is actually requested.
	 */
	private String resourcePath;

	/**
	 * The style to use when locating the image resource.
	 */
	private String style;

	/**
	 * The locale to use when locating the image resource
	 */
	private Locale locale;

    /**
     * @see wicket.Component#Component(String)
     */
    public Image(final String name)
    {
        super(name);
    }

    /**
     * @see wicket.Component#Component(String, Serializable)
     */
    public Image(final String name, final Serializable object)
    {
        super(name, object);
    }

    /**
     * @see wicket.Component#Component(String, Serializable, String)
     */
    public Image(final String name, final Serializable object, final String expression)
    {
        super(name, object, expression);
    }

    /**
     * @return Gets the image resource for the component.
     */
    protected IResource getResource()
    {
        if (resourcePath.indexOf("..") != -1 || resourcePath.indexOf("/") != -1)
        {
            throw new WicketRuntimeException("Source for image resource cannot contain a path");
        }

        final String path = Classes.packageName(getPage().getClass()) + "." + resourcePath;
        return Resource.locate
        (
            getApplicationSettings().getSourcePath(),
            getPage().getClass().getClassLoader(),
            path,
            style,
            locale,
            null
        );
    }

    /**
     * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
     */
    protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
    }

    /**
     * @see wicket.Component#onComponentTag(ComponentTag)
     */
    protected void onComponentTag(final ComponentTag tag)
    {
        final String imageResource = getModelObjectAsString();
        if (Strings.isEmpty(imageResource))
        {
            resourcePath = tag.getString("src");
        }
        else
        {
            resourcePath = imageResource;
        }
	    style = getStyle();
	    locale = getLocale();
    	
        super.onComponentTag(tag);
    }

	static
    {
        RequestCycle.registerRequestListenerInterface(IResourceListener.class);
    }
}


