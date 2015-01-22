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
import org.apache.wicket.markup.html.media.MediaStreamingResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * An audio media component to playback audio files.
 * 
 * @author Tobias Soloschenko
 * @author Andrew Lombardi
 * 
 */
public class Audio extends MediaComponent
{

	private static final long serialVersionUID = 1L;

	public Audio(String id)
	{
		super(id);
	}

	public Audio(String id, IModel<?> model)
	{
		super(id, model);
	}

	public Audio(String id, MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		super(id, mediaStreamingResourceReference);
	}

	public Audio(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference)
	{
		super(id, model, mediaStreamingResourceReference);
	}

	public Audio(String id, MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		super(id, mediaStreamingResourceReference, pageParameters);
	}

	public Audio(String id, IModel<?> model,
		MediaStreamingResourceReference mediaStreamingResourceReference,
		PageParameters pageParameters)
	{
		super(id, model, mediaStreamingResourceReference, pageParameters);
	}

	public Audio(String id, String url)
	{
		super(id, url);
	}

	public Audio(String id, IModel<?> model, String url)
	{
		super(id, model, url);
	}

	public Audio(String id, String url, PageParameters pageParameters)
	{
		super(id, url, pageParameters);
	}

	public Audio(String id, IModel<?> model, String url, PageParameters pageParameters)
	{
		super(id, model, url, pageParameters);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		this.checkComponentTag(tag, "audio");
		super.onComponentTag(tag);
	}
}
