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
package org.apache.wicket.request.handler.render;

import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Configures common methods which are used by all tests
 */
class TestPageRenderer extends WebPageRenderer
{
	public RedirectPolicy redirectPolicy;
	public boolean ajax;
	public boolean onePassRender;
	public boolean redirectToRender;
	public boolean shouldPreserveClientUrl;
	public boolean newPageInstance;
	public boolean pageStateless;
	public boolean redirectToBuffer;
	public boolean sessionTemporary;

	public TestPageRenderer()
	{
		this(null);
	}

	public TestPageRenderer(RenderPageRequestHandler handler)
	{
		super(handler);
	}

	@Override
	protected BufferedWebResponse getAndRemoveBufferedResponse(Url url)
	{
		return null;
	}

	@Override
	protected BufferedWebResponse renderPage(Url targetUrl, RequestCycle requestCycle)
	{
		BufferedWebResponse webResponse = super.renderPage(targetUrl, requestCycle);
		webResponse.write("some response".getBytes());
		return webResponse;
	}

	@Override
	protected RedirectPolicy getRedirectPolicy()
	{
		return redirectPolicy;
	}

	@Override
	protected boolean isAjax(RequestCycle requestCycle)
	{
		return ajax;
	}

	@Override
	protected boolean shouldPreserveClientUrl(RequestCycle requestCycle)
	{
		return shouldPreserveClientUrl;
	}

	public boolean isNewPageInstance()
	{
		return newPageInstance;
	}

	public boolean isPageStateless()
	{
		return pageStateless;
	}

	@Override
	protected boolean isOnePassRender()
	{
		return onePassRender;
	}

	@Override
	protected boolean isRedirectToRender()
	{
		return redirectToRender;
	}

	@Override
	protected boolean isRedirectToBuffer()
	{
		return redirectToBuffer;
	}

	@Override
	protected boolean isSessionTemporary()
	{
		return sessionTemporary;
	}
}