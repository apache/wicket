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

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.value.ValueMap;

/**
 * An Image component displays a localizable image resource.
 * <p>
 * For details of how Images load, generate and manage images, see {@link LocalizedImageResource}.
 * 
 * @see NonCachingImage
 * 
 * @author Jonathan Locke
 */
public class Image extends WebComponent implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** The image resource this image component references */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * This constructor can be used if you have a img tag that has a src that points to a
	 * PackageResource (which will be created and bind to the shared resources) Or if you have a
	 * value attribute in your tag for which the image factory can make an image.
	 * 
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public Image(final String id)
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
		ValueMap resourceParameters)
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
	public Image(final String id, final Resource imageResource)
	{
		super(id);
		setImageResource(imageResource);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final IModel model)
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
		this(id, new Model(string));
	}

	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested()
	{
		localizedImageResource.onResourceRequested();
	}

	/**
	 * @param imageResource
	 *            The new ImageResource to set.
	 */
	public void setImageResource(final Resource imageResource)
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
		final ValueMap parameters)
	{
		localizedImageResource.setResourceReference(resourceReference, parameters);
	}

	/**
	 * @see org.apache.wicket.Component#setModel(org.apache.wicket.model.IModel)
	 */
	public Component setModel(IModel model)
	{
		// Null out the image resource, so we reload it (otherwise we'll be
		// stuck with the old model.
		localizedImageResource.setResourceReference(null);
		localizedImageResource.setResource(null);
		return super.setModel(model);
	}

	/**
	 * @return Resource returned from subclass
	 */
	protected Resource getImageResource()
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
	protected IModel initModel()
	{
		// Images don't support Compound models. They either have a simple
		// model, explicitly set, or they use their tag's src or value
		// attribute to determine the image.
		return null;
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "img");
		super.onComponentTag(tag);
		final Resource resource = getImageResource();
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
	}

	/**
	 * @see org.apache.wicket.Component#getStatelessHint()
	 */
	protected boolean getStatelessHint()
	{
		return (getImageResource() == null || getImageResource() == localizedImageResource.getResource()) &&
			localizedImageResource.isStateless();
	}

	/**
	 * @see org.apache.wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}
}
