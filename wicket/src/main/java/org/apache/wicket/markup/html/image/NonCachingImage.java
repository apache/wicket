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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A subclass of {@link Image} that adds random noise to the url every request to prevent the
 * browser from caching the image.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NonCachingImage extends Image
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, IModel)
	 * 
	 * @param id
	 * @param model
	 */
	public NonCachingImage(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 *
	 * @see Image#Image(String, org.apache.wicket.request.resource.IResource)
	 *
	 * @param id
	 * @param imageResource
	 */
	public NonCachingImage(String id, IResource imageResource)
	{
		super(id, imageResource);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, org.apache.wicket.request.resource.ResourceReference, org.apache.wicket.request.mapper.parameter.PageParameters)
	 * 
	 * @param id
	 * @param resourceReference
	 * @param resourceParameters
	 */
	public NonCachingImage(String id, ResourceReference resourceReference,
		PageParameters resourceParameters)
	{
		super(id, resourceReference, resourceParameters);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, ResourceReference)
	 * 
	 * @param id
	 * @param resourceReference
	 */
	public NonCachingImage(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String, String)
	 * 
	 * 
	 * @param id
	 * @param string
	 */
	public NonCachingImage(String id, String string)
	{
		super(id, string);
	}

	/**
	 * Construct.
	 * 
	 * @see Image#Image(String)
	 * 
	 * @param id
	 */
	public NonCachingImage(String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.markup.html.image.Image#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		// the parameter is already added for Ajax requests by the super call
		if (AjaxRequestTarget.get() == null)
		{
			addAntiCacheParameter(tag);
		}
	}

}
