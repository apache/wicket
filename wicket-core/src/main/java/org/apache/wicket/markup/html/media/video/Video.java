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
import org.apache.wicket.markup.html.media.MediaStreamingResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A video media component to display videos.
 * 
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 */
public class Video extends MediaComponent
{

	private static final long serialVersionUID = 1L;

	private Integer width;

	private Integer height;

	private ResourceReference poster;

	private PageParameters posterPageParameters;

	public Video(String id)
	{
		super(id);
	}

	public Video(String id, IModel<?> model)
	{
		super(id, model);
	}

	public Video(String id, MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		super(id, mediaStreamingResourceReference);
	}

	public Video(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		super(id, model, mediaStreamingResourceReference);
	}

	public Video(String id, MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		super(id, mediaStreamingResourceReference, pageParameters);
	}

	public Video(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		super(id, model, mediaStreamingResourceReference, pageParameters);
	}

	public Video(String id, String url)
	{
		super(id, url);
	}

	public Video(String id, IModel<?> model, String url)
	{
		super(id, model, url);
	}

	public Video(String id, String url, PageParameters pageParameters)
	{
		super(id, url, pageParameters);
	}

	public Video(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		super(id, model, url, pageParameters);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "video");
		super.onComponentTag(tag);

		if (width != null)
		{
			tag.put("width", width);
		}

		if (height != null)
		{
			tag.put("height", height);
		}

		if (poster != null)
		{
			tag.put("poster", RequestCycle.get().urlFor(poster, posterPageParameters));
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
