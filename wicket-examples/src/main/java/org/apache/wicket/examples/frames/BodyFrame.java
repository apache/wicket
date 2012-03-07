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

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;


/**
 * Body frame page for the frames example.
 * 
 * @author Eelco Hillenius
 */
public class BodyFrame extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;

	private final FrameTarget frameTarget = new FrameTarget(Page1.class);

	/**
	 * Constructor
	 */
	public BodyFrame()
	{
		// create a new page instance, passing this 'master page' as an argument
		LeftFrame leftFrame = new LeftFrame(this);
		getSession().getPageManager().touchPage(leftFrame);
		// get the url to that page
		IRequestHandler leftFrameHandler = new RenderPageRequestHandler(new PageProvider(leftFrame));
		// and create a simple component that modifies it's src attribute to
		// hold the url to that frame
		ExampleFrame leftFrameTag = new ExampleFrame("leftFrame", leftFrameHandler);
		add(leftFrameTag);

		ExampleFrame rightFrameTag = new ExampleFrame("rightFrame")
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			protected CharSequence getUrl()
			{
				return frameTarget.getUrl();
			}
		};
		add(rightFrameTag);
	}

	/**
	 * Gets frameTarget.
	 * 
	 * @return frameTarget
	 */
	public FrameTarget getFrameTarget()
	{
		return frameTarget;
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		return false;
	}
}