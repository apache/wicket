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

import wicket.IModel;
import wicket.IResourceListener;
import wicket.Page;
import wicket.RenderException;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.HtmlComponent;
import wicket.protocol.http.HttpResponse;
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

/**
 * An image component represents a localizable image resource. The image name comes from
 * the src attribute of the image tag that the component is attached to.
 * @author Jonathan Locke
 */
public class Image extends HtmlComponent implements IResourceListener
{
    /** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	static
    {
        RequestCycle.registerListenerInterface(IResourceListener.class);
    }

    /** loaded resource. */
    private IResource resource;

    /**
     * Constructor without a model; the src tag of the img will be used to find the image
     * resource.
     * @param name The non-null name of this component
     */
    public Image(String name)
    {
        super(name);
    }

    /**
     * Constructor that uses the provided {@link IModel}as its model; the model object
     * will be used to find the image resource. All components have names. A component's
     * name cannot be null.
     * @param name The non-null name of this component
     * @param model the model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public Image(String name, IModel model)
    {
        super(name, model);
    }

    /**
     * Constructor that uses the provided instance of {@link IModel}as a dynamic model;
     * the model object will be used to find the image resource. This model will be
     * wrapped in an instance of {@link wicket.PropertyModel}
     * using the provided expression.
     * Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(myIModel, expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param model the instance of {@link IModel}from which the model object will be
     *            used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public Image(String name, IModel model, String expression)
    {
        super(name, model, expression);
    }

    /**
     * Constructor that uses the provided object as a simple model; the model object will
     * be used to find the image resource. This object will be wrapped in an instance of
     * {@link wicket.Model}. All components have names.
     * A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as a simple model
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public Image(String name, Serializable object)
    {
        super(name, object);
    }

    /**
     * Constructor that uses the provided object as a dynamic model; the model object will
     * be used to find the image resource. This object will be wrapped in an instance of
     * {@link wicket.Model} that will be wrapped in an instance of
     * {@link wicket.PropertyModel} using
     * the provided expression. Thus, using this constructor is a short-hand for:
     * 
     * <pre>
     * new MyComponent(name, new PropertyModel(new Model(object), expression));
     * </pre>
     * 
     * All components have names. A component's name cannot be null.
     * @param name The non-null name of this component
     * @param object the object that will be used as the subject for the given expression
     * @param expression the OGNL expression that works on the given object
     * @throws RenderException Thrown if the component has been given a null name.
     */
    public Image(String name, Serializable object, String expression)
    {
        super(name, object, expression);
    }

    /**
     * @param source The source attribute of the image tag
     * @return Gets the image resource to attach to the component.
     */
    protected IResource getImageResource(final String source)
    {
        if ((source.indexOf("..") != -1) || (source.indexOf("/") != -1))
        {
            throw new RenderException("Source for resource image cannot contain a path");
        }

        final String path = Classes.packageName(getPage().getClass()) + "." + source;

        return Resource.locate(getApplicationSettings().getSourcePath(), getPage().getClass()
                .getClassLoader(), path, getStyle(), getLocale(), null);
    }

    /**
     * @see wicket.Component#handleComponentTag(RequestCycle, ComponentTag)
     */
    protected void handleComponentTag(RequestCycle cycle, ComponentTag tag)
    {
        checkTag(tag, "img");
        super.handleComponentTag(cycle, tag);

        final String resourceToLoad;
        String imageResource = (String) getModelObject();

        if (imageResource != null)
        {
            resourceToLoad = imageResource;
        }
        else
        {
            resourceToLoad = tag.getString("src");
        }

        this.resource = getImageResource(resourceToLoad);

        if (this.resource == null)
        {
            throw new RenderException("Could not find image resource " + resourceToLoad);
        }

        tag.put("src", cycle.urlFor(this, IResourceListener.class));
    }

    /**
     * @see wicket.Component#handleBody(RequestCycle, MarkupStream,
     *      ComponentTag)
     */
    protected void handleBody(RequestCycle cycle, MarkupStream markupStream, ComponentTag openTag)
    {
    }

    /**
     * @see wicket.IResourceListener#resourceRequested(wicket.RequestCycle)
     */
    public void resourceRequested(final RequestCycle cycle)
    {
        // The cycle's page is set to null so that it won't be rendered back to
        // the client
        // since the resource being requested has nothing to do with pages
        cycle.setPage((Page) null);

        // Respond with image
        final HttpServletResponse response = ((HttpResponse) cycle.getResponse())
                .getServletResponse();

        response.setContentType("image/" + resource.getExtension());

        try
        {
            final OutputStream out = new BufferedOutputStream(response.getOutputStream());

            try
            {
                Streams.writeStream(new BufferedInputStream(resource.getInputStream()), out);
            }
            finally
            {
                resource.close();
                out.flush();
            }
        }
        catch (IOException e)
        {
            throw new RenderException("Unable to render resource " + resource, e);
        }
        catch (ResourceNotFoundException e)
        {
            throw new RenderException("Unable to render resource " + resource, e);
        }
    }
}

///////////////////////////////// End of File /////////////////////////////////
