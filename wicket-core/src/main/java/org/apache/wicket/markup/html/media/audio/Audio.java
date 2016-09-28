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
package org.apache.wicket.markup.html.media.audio;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.media.MediaComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * An audio media component to playback audio files.
 *
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * @since 7.0.0
 */
public class Audio extends MediaComponent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 */
	public Audio(String id)
	{
		super(id);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 */
	public Audio(String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference of the audio file
	 */
	public Audio(String id, ResourceReference resourceReference)
	{
		super(id, resourceReference);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the audio file
	 */
	public Audio(String id, IModel<?> model, ResourceReference resourceReference)
	{
		super(id, model, resourceReference);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param resourceReference
	 *            the resource reference of the audio file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the audio URL
	 */
	public Audio(String id, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		super(id, resourceReference, pageParameters);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param resourceReference
	 *            the resource reference of the audio file
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the audio URL
	 */
	public Audio(String id, IModel<?> model, ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		super(id, model, resourceReference, pageParameters);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to be used for the audio component
	 */
	public Audio(String id, String url)
	{
		super(id, url);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the audio component
	 */
	public Audio(String id, IModel<?> model, String url)
	{
		super(id, model, url);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param url
	 *            an external URL to be used for the audio component
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the audio URL
	 */
	public Audio(String id, String url, PageParameters pageParameters)
	{
		super(id, null, url, pageParameters);
	}

	/**
	 * Creates an audio component
	 *
	 * @param id
	 *            the component id
	 * @param model
	 *            the internally used model
	 * @param url
	 *            an external URL to be used for the audio component
	 * @param pageParameters
	 *            the page parameters to be used to be prepended to the audio URL
	 */
	public Audio(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		super(id, model, url, pageParameters);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		checkComponentTag(tag, "audio");
		super.onComponentTag(tag);
	}
}
