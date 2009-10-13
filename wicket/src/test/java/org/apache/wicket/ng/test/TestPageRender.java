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
package org.apache.wicket.ng.test;

import junit.framework.TestCase;

import org.apache.wicket.ng.Page;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.markup.html.link.ILinkListener;
import org.apache.wicket.ng.markup.html.link.Link;
import org.apache.wicket.ng.mock.MockApplication;
import org.apache.wicket.ng.mock.MockRequest;
import org.apache.wicket.ng.mock.MockRequestCycle;
import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.mapper.MountedMapper;
import org.apache.wicket.ng.request.response.Response;
import org.apache.wicket.ng.request.response.StringResponse;

public class TestPageRender extends TestCase
{
	public static class Page1 extends Page
	{
		private static final long serialVersionUID = 1L;

		public Page1()
		{
			Link l;
			add(l = new Link("link")
			{
				private static final long serialVersionUID = 1L;

				public void onLinkClicked()
				{
					System.out.println("Link clicked!");
				}
			});
			l.setLabel("A Link!");
		}

		@Override
		public void renderPage()
		{
			super.renderPage();
		}
	};

	public void testRender1()
	{
		// Store current ThreadContext
		ThreadContext context = ThreadContext.getAndClean();

		// Create MockApplication
		MockApplication app = new MockApplication();
		app.setName("TestApplication1");
		app.set(); // set it to ThreadContext
		app.initApplication();

		// Mount the test page
		app.registerEncoder(new MountedMapper("first-test-page", Page1.class));

		// Construct request for first-test-page
		Request request = new MockRequest(Url.parse("first-test-page"));
		Response response = new StringResponse();

		// create and execute request cycle
		MockRequestCycle cycle = (MockRequestCycle)app.createRequestCycle(request, response);
		cycle.processRequestAndDetach();

		System.out.println("Rendered:");
		System.out.println(response);

		// invoke listener on the page
		request = new MockRequest(Url.parse("wicket/page?0-1.ILinkListener-link"));
		response = new StringResponse();
		cycle = (MockRequestCycle)app.createRequestCycle(request, response);
		cycle.processRequestAndDetach();

		// invoke the listener again - without parsing the URL
		cycle = (MockRequestCycle)app.createRequestCycle(request, response);
		cycle.forceRequestHandler(new ListenerInterfaceRequestHandler(new PageAndComponentProvider(
			0, null, "link"), ILinkListener.INTERFACE));
		cycle.processRequestAndDetach();

		// cleanup
		app.destroy();
		ThreadContext.restore(context);
	}
}
