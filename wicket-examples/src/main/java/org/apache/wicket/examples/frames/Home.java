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
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;

/**
 * Home page for the frames example.
 * 
 * @author Eelco Hillenius
 */
public class Home extends WebPage
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public Home()
	{
		IRequestHandler topFrameHandler = new BookmarkablePageRequestHandler(new PageProvider(
			TopFrame.class));
		ExampleFrame topFrame = new ExampleFrame("topFrame", topFrameHandler);
		add(topFrame);

		IRequestHandler bodyFrameHandler = new BookmarkablePageRequestHandler(new PageProvider(
			BodyFrame.class));
		ExampleFrame bodyFrame = new ExampleFrame("bodyFrame", bodyFrameHandler);
		add(bodyFrame);
	}
}