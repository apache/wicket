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

import javax.servlet.http.HttpServletResponse;

import wicket.IResourceListener;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.protocol.http.WebResponse;
import wicket.util.io.Streams;
import wicket.util.lang.Classes;
import wicket.util.resource.IResource;
import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Locale;

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
public class Image extends WebComponent implements IResourceListener
{
    /** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	/**
	 * The string representation of the resource to be loaded. Actual
	 * resource objects are not allocated until the image is
	 * actually requested.
	 */
	private String resourceToLoad;

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
     * Implementation of IResourceListener.  Renders resource back to requester.
     * @see wicket.IResourceListener#resourceRequested()
     */
    public void resourceRequested()
    {
	    // Obtain the resource
	    IResource image = getResource(resourceToLoad);
	    if (image == null)
	    {
	        throw new WicketRuntimeException("Could not find image resource " + resourceToLoad);
	    }

        // Get request cycle
        final RequestCycle cycle = getRequestCycle();

        // The cycle's page is set to null so that it won't be rendered back to
        // the client since the resource being requested has nothing to do with pages
        cycle.setPage((Page)null);

        // Respond with image
        final HttpServletResponse response = ((WebResponse)cycle.getResponse()).getHttpServletResponse();
        response.setContentType("image/" + image.getExtension());

        try
        {
            final OutputStream out = new BufferedOutputStream(response.getOutputStream());
            try
            {
                Streams.writeStream(new BufferedInputStream(image.getInputStream()), out);
            }
            finally
            {
                image.close();
                out.flush();
            }
        }
        catch (IOException e)
        {
            throw new WicketRuntimeException("Unable to render resource " + image, e);
        }
        catch (ResourceNotFoundException e)
        {
            throw new WicketRuntimeException("Unable to render resource " + image, e);
        }
    }

    /**
     * @param source The source attribute of the image tag
     * @return Gets the image resource for the component.
     */
    protected IResource getResource(final String source)
    {
        if (source.indexOf("..") != -1 || source.indexOf("/") != -1)
        {
            throw new WicketRuntimeException("Source for image resource cannot contain a path");
        }

        final String path = Classes.packageName(getPage().getClass()) + "." + source;
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
     * @see wicket.Component#handleComponentTagBody(MarkupStream, ComponentTag)
     */
    protected void handleComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
    {
    }

    /**
     * @see wicket.Component#handleComponentTag(ComponentTag)
     */
    protected void handleComponentTag(final ComponentTag tag)
    {
        checkComponentTag(tag, "img");
        super.handleComponentTag(tag);

        final String imageResource = (String)getModelObject();

        if (imageResource != null)
        {
            resourceToLoad = imageResource;
        }
        else
        {
            resourceToLoad = tag.getString("src");
        }
	    style = getStyle();
	    locale = getLocale();

        final String url = getRequestCycle().urlFor(this, IResourceListener.class);
		tag.put("src", url.replaceAll("&", "&amp;"));
    }

	static
    {
        RequestCycle.registerRequestListenerInterface(IResourceListener.class);
    }
}


