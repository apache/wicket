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
package org.apache.wicket;

import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.StalePageException;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IExceptionSettings.UnexpectedExceptionDisplay;

public class DefaultExceptionMapper implements IExceptionMapper
{

	public IRequestHandler map(Exception e)
	{
		if (e instanceof StalePageException)
		{
			// If the page was stale, just rerender it
			return new RenderPageRequestHandler(new PageProvider(((StalePageException)e).getPage()));
		}
		else if (e instanceof PageExpiredException)
		{
			return new RenderPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getPageExpiredErrorPage()));
		}
		else
		{
			final Application application = Application.get();
			final UnexpectedExceptionDisplay unexpectedExceptionDisplay = application.getExceptionSettings()
				.getUnexpectedExceptionDisplay();

			if (IExceptionSettings.SHOW_EXCEPTION_PAGE.equals(unexpectedExceptionDisplay))
			{
				return new RenderPageRequestHandler(new PageProvider(
				// TODO WICKET-NG How to provide the page to ExceptionErrorPage ?!
					new ExceptionErrorPage(e, null)));
			}
			else if (IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE.equals(unexpectedExceptionDisplay))
			{
				return new RenderPageRequestHandler(new PageProvider(
					application.getApplicationSettings().getInternalErrorPage()));
			}
			else
			{
				// IExceptionSettings.SHOW_NO_EXCEPTION_PAGE
				return new EmptyRequestHandler();
			}
		}
	}
}
