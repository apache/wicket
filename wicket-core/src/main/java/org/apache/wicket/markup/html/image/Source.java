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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A component which displays localizable image resources within a picture tag.
 * 
 * The source tag is the same as the image element, but it is also possible to set the media
 * attribute with setMedia(String media). The second difference is that there is no src attribute,
 * so every ResourceReference and ImageResource is added directly to the srcset attribute.
 * 
 * @see org.apache.wicket.markup.html.image.Image
 * @author Tobias Soloschenko
 * 
 */
public class Source extends Image
{

	private static final long serialVersionUID = 1L;

	private String media;

	/**
	 * @see org.apache.wicket.markup.html.image.Image
	 */
	protected Source(final String id)
	{
		super(id);
	}

	/**
	 * @see org.apache.wicket.markup.html.image.Image
	 */
	public Source(final String id, final ResourceReference... resourceReferences)
	{
		super(id, null, resourceReferences);
	}

	/**
	 * @see org.apache.wicket.markup.html.image.Image
	 */
	public Source(final String id, PageParameters resourceParameters,
		final ResourceReference... resourceReferences)
	{
		super(id, null, resourceParameters, resourceReferences);
	}

	/**
	 * @see org.apache.wicket.markup.html.image.Image
	 */
	public Source(final String id, final IResource... imageResources)
	{
		super(id, null, imageResources);
	}

	/**
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public Source(final String id, final IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * @see org.apache.wicket.markup.html.image.Image
	 */
	public Source(final String id, final String string)
	{
		super(id, string);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "source");
		super.onComponentTag(tag);
		if (this.media != null)
		{
			tag.put("media", getMedia());
		}
	}

	/**
	 * Sets the media attribute information
	 * 
	 * @param media
	 *            the media attribute information
	 */
	public void setMedia(String media)
	{
		this.media = media;
	}

	/**
	 * Gets the media attribute information
	 * 
	 * @return the media attribute information
	 */
	public String getMedia()
	{
		return media;
	}
}
