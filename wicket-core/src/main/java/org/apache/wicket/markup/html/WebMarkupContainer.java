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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

/**
 * A container of HTML markup and components. It is very similar to the base class MarkupContainer,
 * except that it assumes that it is used in a web environment. Provides a convenience methods to get
 * the current web objects (page, response, request, session).
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * 
 */
public class WebMarkupContainer extends MarkupContainer
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainer(final String id)
	{
		this(id, null);
	}

	/**
	 * @see Component#Component(String, IModel)
	 */
	public WebMarkupContainer(final String id, IModel<?> model)
	{
		super(id, model);
	}

	/**
	 * A convenience method to return the WebPage. Same as getPage().
	 * 
	 * @return WebPage
	 */
	public final WebPage getWebPage()
	{
		return (WebPage)getPage();
	}

	/**
	 * A convenience method to return the current WebRequest. Same as {@link org.apache.wicket.Component#getRequest()}.
	 *
	 * @return the current WebRequest
	 */
	public final WebRequest getWebRequest()
	{
		return (WebRequest)getRequest();
	}

	/**
	 * A convenience method to return the current WebResponse. Same as {@link org.apache.wicket.Component#getResponse()}.
	 *
	 * @return the current WebResponse
	 */
	public final WebResponse getWebResponse()
	{
		return (WebResponse)getResponse();
	}

	/**
	 * A convenience method to return the WebSession. Same as {@link org.apache.wicket.Component#getSession()} .
	 *
	 * @return the current WebSession
	 */
	public final WebSession getWebSession()
	{
		return (WebSession)getSession();
	}
}