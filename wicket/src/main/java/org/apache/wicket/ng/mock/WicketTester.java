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
package org.apache.wicket.ng.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.request.IRequestHandler;
import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.DefaultPageProvider;
import org.apache.wicket.ng.request.handler.IPageProvider;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.ng.settings.IRequestCycleSettings.RenderStrategy;

/**
 * Experimental implementation
 * 
 * @author Matej Knopp
 */
public class WicketTester
{
	private final MockApplication application;

	private boolean followRedirects = true;

	private MockWebRequest lastRequest;
	private MockWebResponse lastResponse;

	private final List<MockWebRequest> previousRequests = new ArrayList<MockWebRequest>();
	private final List<MockWebResponse> previousResponses = new ArrayList<MockWebResponse>();

	private final ThreadContext oldThreadContext;

	/**
	 * Creates a new {@link WicketTester} instance. It also binds it's Application to current thread
	 * so all activities that needs the original application (if there was one bound to current
	 * thread) need to wait until {@link #destroy()} is invoked.
	 */
	public WicketTester()
	{
		oldThreadContext = ThreadContext.getAndClean();

		application = new MockApplication();
		application.setName("WicketTesterApplication");
		application.set();
		application.initApplication();
	}

	/**
	 * Destroys the tester. Restores {@link ThreadContext} to state before instance of
	 * {@link WicketTester} was created.
	 */
	public void destroy()
	{
		ThreadContext.restore(oldThreadContext);
		application.destroy();
	}

	int redirectCount;

	/**
	 * Processes the request in mocked Wicket environment.
	 * 
	 * @param request
	 *            request to process
	 * 
	 * @param forcedRequestHandler
	 *            optional parameter to override parsing the request URL and force
	 *            {@link IRequestHandler}
	 */
	public void processRequest(MockWebRequest request, IRequestHandler forcedRequestHandler)
	{
		try
		{
			if (lastRequest != null)
			{
				previousRequests.add(lastRequest);
			}
			if (lastResponse != null)
			{
				previousResponses.add(lastResponse);
			}

			lastRequest = request;
			lastResponse = new MockWebResponse();

			MockRequestCycle cycle = (MockRequestCycle)application.createRequestCycle(request,
				lastResponse);

			if (forcedRequestHandler != null)
			{
				cycle.forceRequestHandler(forcedRequestHandler);
			}

			cycle.processRequestAndDetach();

			if (followRedirects && lastResponse.isRedirect())
			{
				if (redirectCount == 100)
				{
					throw new IllegalStateException(
						"Possible infinite redirect detected. Bailing out.");
				}
				++redirectCount;
				Url newUrl = Url.parse(lastResponse.getRedirectUrl());
				if (newUrl.isAbsolute())
				{
					throw new WicketRuntimeException("Can not follow absolute redirect URL.");
				}

				// append redirect URL to current URL (what browser would do)
				Url mergedURL = new Url(request.getUrl().getSegments(), newUrl.getQueryParameters());
				mergedURL.concatSegments(newUrl.getSegments());

				processRequest(new MockWebRequest(mergedURL), null);

				--redirectCount;
			}
		}
		finally
		{
			redirectCount = 0;
		}
	}

	/**
	 * Renders the page specified by given {@link IPageProvider}. After render the page instance can
	 * be retreived using {@link #getLastRenderedPage()} and the rendered document will be available
	 * in {@link #getLastResponse()}.
	 * 
	 * Depending on {@link RenderStrategy} invoking this method can mean that a redirect will happen
	 * before the actual render.
	 * 
	 * @param pageProvider
	 */
	public void startPage(IPageProvider pageProvider)
	{
		MockWebRequest request = new MockWebRequest(new Url());
		IRequestHandler handler = new RenderPageRequestHandler(pageProvider);
		processRequest(request, handler);
	}

	/**
	 * Renders the page.
	 * 
	 * @see #startPage(IPageProvider)
	 * 
	 * @param page
	 */
	public void startPage(Page page)
	{
		startPage(new DefaultPageProvider(page));
	}

	/**
	 * Simulates processing URL that invokes specified {@link RequestListenerInterface} on
	 * component.
	 * 
	 * After the listener interface is invoked the page containing the component will be rendered
	 * (with an optional redirect - depending on {@link RenderStrategy}).
	 * 
	 * @param component
	 * @param listener
	 */
	public void executeListener(Component component, RequestListenerInterface listener)
	{
		// there are two ways to do this. RequestCycle could be forced to call the handler
		// directly but constructing and parsing the URL increases the chance of triggering bugs
		IRequestHandler handler = new ListenerInterfaceRequestHandler(new PageAndComponentProvider(
			component.getPage(), component), listener);

		Url url = urlFor(handler);

		processRequest(new MockWebRequest(url), null);
	}

	/**
	 * @return last request or <code>null</code> if no request has happened yet.
	 */
	public MockWebRequest getLastRequest()
	{
		return lastRequest;
	}

	/**
	 * @return last response or <code>null</code>> if no response has been produced yet.
	 */
	public MockWebResponse getLastResponse()
	{
		return lastResponse;
	}

	/**
	 * @return list of prior requests
	 */
	public List<MockWebRequest> getPreviousRequests()
	{
		return Collections.unmodifiableList(previousRequests);
	}

	/**
	 * @return list of prior responses
	 */
	public List<MockWebResponse> getPreviousResponses()
	{
		return Collections.unmodifiableList(previousResponses);
	}

	/**
	 * @return last rendered page
	 */
	public Page getLastRenderedPage()
	{
		return (Page)application.getLastRenderedPage();
	}

	/**
	 * Sets whether responses with redirects will be followed automatically.
	 * 
	 * @param followRedirects
	 */
	public void setFollowRedirects(boolean followRedirects)
	{
		this.followRedirects = followRedirects;
	}

	/**
	 * @return <code>true</code> if redirect responses will be followed automatically,
	 *         <code>false</code> otherwise.
	 */
	public boolean isFollowRedirects()
	{
		return followRedirects;
	}

	/**
	 * Encodes the {@link IRequestHandler} to {@link Url}. It should be safe to call this method
	 * outside request thread as log as no registered {@link IRequestMapper} requires a
	 * {@link RequestCycle}.
	 * 
	 * @param handler
	 * @return {@link Url} for handler.
	 */
	public Url urlFor(IRequestHandler handler)
	{
		return application.getRootRequestMapper().mapHandler(handler);
	}

	/**
	 * Returns the {@link MockApplication} for this environment.
	 * 
	 * @return application
	 */
	public MockApplication getApplication()
	{
		return application;
	}
}
