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
package org.apache.wicket.dbsc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.IResource;

import com.github.openjson.JSONObject;

/**
 * Base of the two DBSC endpoints: both take POST only, are never cached and refuse to be embedded.
 */
abstract class Endpoint implements IResource
{
	private static final long serialVersionUID = 1L;

	@Override
	public final void respond(Attributes attributes)
	{
		WebRequest request = (WebRequest)attributes.getRequest();
		WebResponse response = (WebResponse)attributes.getResponse();
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("X-Frame-Options", "DENY");
		response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
		if (!"POST".equals(((HttpServletRequest)request.getContainerRequest()).getMethod()))
		{
			response.setHeader("Allow", "POST");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		respond(DeviceBoundSessions.get(), request, response);
	}

	/**
	 * Answers a POST.
	 */
	abstract void respond(DeviceBoundSessions deviceBoundSessions, WebRequest request,
		WebResponse response);

	static void writeJson(WebResponse response, String json)
	{
		response.setContentType("application/json");
		response.write(json);
	}

	/**
	 * Tells the browser to end its DBSC session.
	 */
	static void terminate(WebResponse response)
	{
		writeJson(response, new JSONObject().put("continue", false).toString());
	}
}
