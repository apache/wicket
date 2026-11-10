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
package org.apache.wicket.examples.frames;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.IRequestHandler;

/**
 * A simple component setting page URL in the src frame tag attribute.
 * 
 * @author Pedro Santos
 */
public class ExampleFrame extends WebComponent
{
	/** */
	private static final long serialVersionUID = 1L;

	private CharSequence url;

	/**
	 * @param id
	 */
	public ExampleFrame(String id)
	{
		super(id);
	}

	/**
	 * @param id
	 * @param requestHandler
	 */
	public ExampleFrame(String id, IRequestHandler requestHandler)
	{
		super(id);
		url = urlFor(requestHandler);
	}

	/**
	 * @param id
	 * @param url
	 */
	public ExampleFrame(String id, CharSequence url)
	{
		super(id);
		this.url = url;
	}

	/**
	 * Handles this frame's tag.
	 * 
	 * @param tag
	 *            the component tag
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "frame");

		tag.put("src", getUrl());

		super.onComponentTag(tag);
	}

	/**
	 * @return src URL
	 */
	protected CharSequence getUrl()
	{
		return url;
	}

}
