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
package org.apache.wicket.settings;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.authorization.IUnauthorizedResourceRequestListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;

/**
 * An IUnauthorizedResourceRequestListener that schedules a response with status code 403 (Forbidden)
 */
public class DefaultUnauthorizedResourceRequestListener implements IUnauthorizedResourceRequestListener
{
	@Override
	public void onUnauthorizedRequest(IResource resource, PageParameters parameters)
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			IRequestHandler handler = new ErrorCodeRequestHandler(HttpServletResponse.SC_FORBIDDEN, createErrorMessage(resource, parameters));
			cycle.replaceAllRequestHandlers(handler);
		}
	}

	protected String createErrorMessage(IResource resource, PageParameters parameters)
	{
		return new StringBuilder()
			.append("The request to resource '")
			.append(resource)
			.append("' with parameters '")
			.append(parameters)
			.append("' cannot be authorized.")
			.toString();
	}

}
