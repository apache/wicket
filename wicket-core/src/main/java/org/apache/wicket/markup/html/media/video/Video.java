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
package org.apache.wicket.markup.html.media.video;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.media.MediaComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A video media component to display videos.
 *
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * @since 7.0.0
 */
public class Video extends MediaComponent
{
	private static final long serialVersionUID = 1L;

	private Integer width;

	private Integer height;

	private ResourceReference poster;

	private PageParameters posterPageParameters;

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 */
	public Video(String id)
	{
		super(id);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 */
	public Video(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference of the video file
	 */
	public Video(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the video file
	 */
	public Video(String id, IModel<?> model, ResourceReference resourceReference)
	{
		super(id, model, resourceReference);
	}

	/**
	 * Creates a media component
	 *
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference of the video file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the video URL
	 */
	public Video(String id, ResourceReference resourceReference, PageParameters pageParameters)
	{
		super(id, resourceReference, pageParameters);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the video file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the video URL
	 */
	public Video(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		super(id, model, resourceReference, pageParameters);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to be used for the video component
	 */
	public Video(String id, String url)
	{
		super(id, url);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the video component
	 */
	public Video(String id, IModel<?> model, String url)
	{
		super(id, model, url);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to be used for the video component
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the video URL
	 */
	public Video(String id, String url, PageParameters pageParameters)
	{
		super(id, null, url, pageParameters);
	}

	/**
	 * Creates a video component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the video component
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the video URL
	 */
	public Video(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		super(id, model, url, pageParameters);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "video");
		super.onComponentTag(tag);

		Integer _width = getWidth();
		if (_width != null)
		{
			tag.put("width", _width);
		}

		Integer _height = getHeight();
		if (_height != null)
		{
			tag.put("height", _height);
		}

		ResourceReference _poster = getPoster();
		if (_poster != null)
		{
			tag.put("poster", RequestCycle.get().urlFor(_poster, getPosterPageParameters()));
		}
	}

	/**
	 * The image to be displayed if the video isn't available
	 *
	 * @return the resource reference of the image
	 */
	public ResourceReference getPoster()
	{
		return poster;
	}

	/**
	 * Gets the posters page parameters
	 *
	 * @return the page parameters for the poster
	 */
	public PageParameters getPosterPageParameters()
	{
		return posterPageParameters;
	}

	/**
	 * Sets the posters page parameters
	 * 
	 * @param posterPageParameters
	 *            the page parameters for the poster
	 */
	public void setPosterPageParameters(PageParameters posterPageParameters)
	{
		this.posterPageParameters = posterPageParameters;
	}

	/**
	 * Sets the image to be displayed if the video isn't available
	 *
	 * @param poster
	 *            the resource reference of the image used if the video isn't available
	 */
	public void setPoster(ResourceReference poster)
	{
		this.poster = poster;
	}

	/**
	 * Sets the image to be displayed if the video isn't available
	 *
	 * @param poster
	 *            the resource reference of the image used if the video isn't available
	 * @param posterPageParameters
	 *            the page parameters for the poster
	 */
	public void setPoster(ResourceReference poster, PageParameters posterPageParameters)
	{
		this.poster = poster;
		this.posterPageParameters = posterPageParameters;
	}

	/**
	 * Gets the width of the video area
	 *
	 * @return the width of the video area
	 */
	public Integer getWidth()
	{
		return width;
	}

	/**
	 * Sets the width of the video area
	 *
	 * @param width
	 *            the width of the video area
	 */
	public void setWidth(Integer width)
	{
		this.width = width;
	}

	/**
	 * Gets the height of the video area
	 *
	 * @return the height of the video area
	 */
	public Integer getHeight()
	{
		return height;
	}

	/**
	 * Sets the height of the video area
	 *
	 * @param height
	 *            the height of the video area
	 */
	public void setHeight(Integer height)
	{
		this.height = height;
	}
}
