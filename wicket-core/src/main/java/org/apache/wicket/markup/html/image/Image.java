/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.image;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * An Image component displays a localizable image resource.
 * <p>
 * For details of how Images load, generate and manage images, see {@link LocalizedImageResource}.
 * 
 * @see NonCachingImage
 * 
 * @author Jonathan Locke
 * 
 */
public class Image extends WebComponent implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** The image resource this image component references */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * This constructor can be used if you override {@link #getImageResourceReference()} or
	 * {@link #getImageResource()}
	 * 
	 * @param id
	 */
	protected Image(final String id)
	{
		super(id);
	}

	/**
	 * Constructs an image from an image resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * Resource that isn't guaranteed to be on every server, for example a dynamic image or
	 * resources that aren't added with a IInitializer at application startup. Then if only that
	 * resource is requested from another server, without the rendering of the page, the image won't
	 * be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 */
	public Image(final String id, final ResourceReference resourceReference)
	{
		this(id, resourceReference, null);
	}

	/**
	 * Constructs an image from an image resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * Resource that isn't guaranteed to be on every server, for example a dynamic image or
	 * resources that aren't added with a IInitializer at application startup. Then if only that
	 * resource is requested from another server, without the rendering of the page, the image won't
	 * be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 * @param resourceParameters
	 *            The resource parameters
	 */
	public Image(final String id, final ResourceReference resourceReference,
		PageParameters resourceParameters)
	{
		super(id);
		setImageResourceReference(resourceReference, resourceParameters);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * This one doesn't have the 'non sticky session clustering' problem that the ResourceReference
	 * constructor has. But this will result in a non 'stable' url and the url will have request
	 * parameters.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public Image(final String id, final IResource imageResource)
	{
		super(id);
		setImageResource(imageResource);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            See Component
	 * @param string
	 *            Name of image
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final String string)
	{
		this(id, new Model<>(string));
	}

	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	@Override
	public void onResourceRequested()
	{
		localizedImageResource.onResourceRequested(null);
	}

	/**
	 * @param imageResource
	 *            The new ImageResource to set.
	 */
	public void setImageResource(final IResource imageResource)
	{
		localizedImageResource.setResource(imageResource);
	}

	/**
	 * @param resourceReference
	 *            The shared ImageResource to set.
	 */
	public void setImageResourceReference(final ResourceReference resourceReference)
	{
		localizedImageResource.setResourceReference(resourceReference);
	}

	/**
	 * @param resourceReference
	 *            The shared ImageResource to set.
	 * @param parameters
	 *            Set the resource parameters for the resource.
	 */
	public void setImageResourceReference(final ResourceReference resourceReference,
		final PageParameters parameters)
	{
		localizedImageResource.setResourceReference(resourceReference, parameters);
	}

	/**
	 * @see org.apache.wicket.Component#setDefaultModel(org.apache.wicket.model.IModel)
	 */
	@Override
	public Component setDefaultModel(IModel<?> model)
	{
		// Null out the image resource, so we reload it (otherwise we'll be
		// stuck with the old model.
		localizedImageResource.setResourceReference(null);
		localizedImageResource.setResource(null);
		return super.setDefaultModel(model);
	}

	/**
	 * @return Resource returned from subclass
	 */
	protected IResource getImageResource()
	{
		return localizedImageResource.getResource();
	}

	/**
	 * @return ResourceReference returned from subclass
	 */
	protected ResourceReference getImageResourceReference()
	{
		return localizedImageResource.getResourceReference();
	}

	/**
	 * @see org.apache.wicket.Component#initModel()
	 */
	@Override
	protected IModel<?> initModel()
	{
		// Images don't support Compound models. They either have a simple
		// model, explicitly set, or they use their tag's src or value
		// attribute to determine the image.
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		super.onComponentTag(tag);
		final IResource resource = getImageResource();
		if (resource != null)
		{
			localizedImageResource.setResource(resource);
		}
		final ResourceReference resourceReference = getImageResourceReference();
		if (resourceReference != null)
		{
			localizedImageResource.setResourceReference(resourceReference);
		}
		localizedImageResource.setSrcAttribute(tag);

		if (shouldAddAntiCacheParameter())
		{
			addAntiCacheParameter(tag);
		}
	}

	/**
	 * Adding an image to {@link org.apache.wicket.ajax.AjaxRequestTarget} most of the times mean that the image has
	 * changes and must be re-rendered.
	 * <p>
	 * With this method the user may change this default behavior for some of her images.
	 * </p>
	 * 
	 * @return {@code true} to add the anti cache request parameter, {@code false} - otherwise
	 */
	protected boolean shouldAddAntiCacheParameter()
	{
		return getRequestCycle().find(AjaxRequestTarget.class) != null;
	}

	/**
	 * Adds random noise to the url every request to prevent the browser from caching the image.
	 * 
	 * @param tag
	 */
	protected final void addAntiCacheParameter(final ComponentTag tag)
	{
		String url = tag.getAttributes().getString("src");
		url = url + (url.contains("?") ? "&" : "?");
		url = url + "antiCache=" + System.currentTimeMillis();

		tag.put("src", url);
	}

	/**
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		return (getImageResource() == null || getImageResource() == localizedImageResource.getResource()) &&
			localizedImageResource.isStateless();
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}

	@Override
	public boolean canCallListenerInterface(Method method)
	{
		boolean isResource = method != null && IResourceListener.class.isAssignableFrom(method.getDeclaringClass());
		if (isResource && isVisibleInHierarchy())
		{
			// when the image data is requested we do not care if this component is enabled in
			// hierarchy or not, only that it is visible
			return true;
		}
		else
		{
			return super.canCallListenerInterface(method);
		}
	}
}
