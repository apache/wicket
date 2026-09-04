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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.resource.LocalizedImageResource;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;


/**
 * &lt;input type="image"&gt; component - like {@link Button} only with an image.
 * <p>
 * For details of how ImageButtons load, generate and manage images, see
 * {@link LocalizedImageResource}.
 * 
 * @author Jonathan Locke
 */
public class ImageButton extends Button implements IRequestListener
{
	private static final long serialVersionUID = 1L;

	/** The image resource this image component references */
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * Constructs an image button from an image <code>ResourceReference</code>. That resource
	 * reference will bind its resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * <code>Resource</code> that isn't guaranteed to be on every server, for example a dynamic
	 * image or resources that aren't added with a <code>IInitializer</code> at application startup.
	 * Then if only that resource is requested from another server, without the rendering of the
	 * page, the image won't be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 */
	public ImageButton(final String id, final ResourceReference resourceReference)
	{
		this(id, resourceReference, null);
	}

	/**
	 * Constructs an image button from an image <code>ResourceReference</code>. That resource
	 * reference will bind its resource to the current SharedResources.
	 * 
	 * If you are using non sticky session clustering and the resource reference is pointing to a
	 * <code>Resource</code> that isn't guaranteed to be on every server, for example a dynamic
	 * image or resources that aren't added with a <code>IInitializer</code> at application startup.
	 * Then if only that resource is requested from another server, without the rendering of the
	 * page, the image won't be there and will result in a broken link.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared image resource
	 * @param resourceParameters
	 *            The resource parameters
	 */
	public ImageButton(final String id, final ResourceReference resourceReference,
		PageParameters resourceParameters)
	{
		super(id);
		setImageResourceReference(resourceReference, resourceParameters);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * This one doesn't have the 'non sticky session clustering' problem that the
	 * <code>ResourceReference</code> constructor has. But this will result in a non 'stable' url
	 * and the url will have request parameters.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public ImageButton(final String id, final IResource imageResource)
	{
		super(id);
		setImageResource(imageResource);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public ImageButton(final String id, final IModel<String> model)
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
	public ImageButton(final String id, final String string)
	{
		this(id, new Model<String>(string));
	}

	@Override
	public boolean rendersPage()
	{
		return false;
	}

	@Override
	public void onRequest()
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
	public ImageButton setDefaultModel(IModel<?> model)
	{
		// Null out the image resource, so we reload it (otherwise we'll be
		// stuck with the old model.
		localizedImageResource.setResourceReference(null);
		localizedImageResource.setResource(null);
		return (ImageButton)super.setDefaultModel(model);
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
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "image");

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
		super.onComponentTag(tag);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.Button#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		return getImageResource() == null && localizedImageResource.isStateless();
	}
}
