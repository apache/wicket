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

import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.IExceptionMapper;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.request.mapper.StalePageException;

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
			return new RenderPageRequestHandler(new PageProvider(Application.get()
				.getApplicationSettings()
				.getInternalErrorPage()));
		}
	}

}
