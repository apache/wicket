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
package org.apache.wicket.examples.staticpages;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.response.StringResponse;

/**
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public abstract class CapturingBookmarkablePageRequestTarget extends BookmarkablePageRequestTarget
{
	Class<? extends Page> displayedPageClass;

	/**
	 * @see org.apache.wicket.request.target.component.BookmarkablePageRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	@Override
	public void respond(RequestCycle requestCycle)
	{
		final StringResponse emailResponse = new StringResponse();
		final WebResponse originalResponse = (WebResponse)RequestCycle.get().getResponse();
		RequestCycle.get().setResponse(emailResponse);
		super.respond(requestCycle);
		onCapture(emailResponse);
		RequestCycle.get().setResponse(originalResponse);
		RequestCycle.get().setRequestTarget(new BookmarkablePageRequestTarget(displayedPageClass));
	}

	protected abstract void onCapture(StringResponse emailResponse);

	/**
	 * Construct.
	 * 
	 * @param <C>
	 * @param <D>
	 * 
	 * @param capturedPageClass
	 *            the bookmarkable page to capture for sending in email
	 * @param displayedPageClass
	 *            the bookmarkable page to display in the browser
	 * @param pageParameters
	 *            the page parameters
	 */
	public <C extends Page, D extends Page> CapturingBookmarkablePageRequestTarget(
		Class<C> capturedPageClass, Class<D> displayedPageClass, PageParameters pageParameters)
	{
		super(capturedPageClass, pageParameters);
		this.displayedPageClass = displayedPageClass;
	}

	/**
	 * Construct.
	 * 
	 * @param <C>
	 * @param <D>
	 * 
	 * @param capturedPageClass
	 *            the bookmarkable page to capture for sending in email
	 * @param displayedPageClass
	 *            the bookmarkable page to display in the browser
	 */
	public <C extends Page, D extends Page> CapturingBookmarkablePageRequestTarget(
		Class<C> capturedPageClass, Class<D> displayedPageClass)
	{
		super(capturedPageClass);
		this.displayedPageClass = displayedPageClass;
	}
}
