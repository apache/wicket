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
package org.apache.wicket.islands;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.islands.serializer.IIslandSerializer;
import org.apache.wicket.islands.serializer.IslandSerializationException;
import org.apache.wicket.request.RequestHandlerExecutor.ReplaceHandlerException;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Serves the page-less endpoints. It only accepts a <code>POST</code> with the header the client
 * runtime sends, which a cross-site form cannot send and a cross-origin script cannot send without
 * a CORS preflight that is never granted, and refuses a request whose <code>Sec-Fetch-Site</code>
 * is not <code>same-origin</code>. These checks apply whether or not the application uses
 * resource isolation for its pages.
 * <p>
 * A call made with a handle the session does not know, for example one issued before the session
 * expired, is answered as expired.
 */
final class PagelessEndpointResource extends AbstractResource
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(PagelessEndpointResource.class);

	static final String HEADER = "X-Wicket-Islands";

	static final String HANDLE_HEADER = "X-Wicket-Islands-Handle";

	private static final int MAX_PAYLOAD = 1024 * 1024;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes)
	{
		ResourceResponse response = new ResourceResponse();
		response.disableCaching();

		WebRequest request = (WebRequest)attributes.getRequest();
		HttpServletRequest http = (HttpServletRequest)request.getContainerRequest();
		if (!"POST".equals(http.getMethod()))
		{
			response.setError(405);
			return response;
		}
		String fetchSite = request.getHeader("Sec-Fetch-Site");
		if (request.getHeader(HEADER) == null ||
			(fetchSite != null && !"same-origin".equals(fetchSite)))
		{
			response.setError(403);
			return response;
		}

		String id = attributes.getParameters().get(0).toString("");
		PagelessEndpoints.Registration registration = IslandSettings.get()
			.getPagelessEndpoints()
			.get(id);
		if (registration == null)
		{
			response.setError(404);
			return response;
		}

		String body;
		try
		{
			body = read(http);
		}
		catch (IOException e)
		{
			response.setError(400);
			return response;
		}

		String handleHeader = request.getHeader(HANDLE_HEADER);
		Serializable handle = handleHeader == null ? null
			: IslandSettings.get().getHandleStore().resolve(id, handleHeader);
		String answer = handleHeader != null && handle == null
			? IslandAnswer.failure(IslandSettings.get().getSerializer(), null, "expired",
				List.of("The page this call belongs to has expired."), List.of())
			: answer(id, registration, new PagelessCall(id, request, handle), body);
		response.setContentType("application/json");
		response.setTextEncoding("UTF-8");
		response.setWriteCallback(new WriteCallback()
		{
			@Override
			public void writeData(Attributes attributes) throws IOException
			{
				attributes.getResponse().write(answer.getBytes(StandardCharsets.UTF_8));
			}
		});
		return response;
	}

	private static String answer(String id, PagelessEndpoints.Registration registration,
		PagelessCall call, String body)
	{
		IIslandSerializer serializer = IslandSettings.get().getSerializer();
		List<String> invalidations = call.getInvalidations();
		try
		{
			if (!registration.authorization().isAllowed(call))
			{
				return IslandAnswer.failure(serializer, null, "forbidden",
					List.of("The call is not allowed."), invalidations);
			}
			Object request = null;
			if (registration.requestType() != Void.class)
			{
				if (!body.isEmpty())
				{
					request = serializer.fromJson(body, registration.requestType());
				}
				if (request == null)
				{
					throw new IslandCallRejectedException("The call has no payload.");
				}
			}
			Object value = registration.endpoint().handle(request, call);
			return IslandAnswer.success(serializer, null, value, invalidations);
		}
		catch (IslandCallRejectedException e)
		{
			return IslandAnswer.failure(serializer, null, "invalid", e.getMessages(), invalidations);
		}
		catch (IslandSerializationException e)
		{
			LOG.debug("Page-less call '{}' has a payload that cannot be converted", id, e);
			return IslandAnswer.failure(serializer, null, "invalid",
				List.of(detail(e, "The call's payload could not be converted.")),
				invalidations);
		}
		catch (AuthorizationException e)
		{
			return IslandAnswer.failure(serializer, null, "forbidden",
				List.of("The call is not allowed."), invalidations);
		}
		catch (ReplaceHandlerException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			LOG.error("Page-less call '{}' failed", id, e);
			return IslandAnswer.failure(serializer, null, "error",
				List.of(detail(e, "The call failed on the server.")), invalidations);
		}
	}

	private static String read(HttpServletRequest request) throws IOException
	{
		try (InputStream in = request.getInputStream())
		{
			byte[] bytes = in.readNBytes(MAX_PAYLOAD + 1);
			if (bytes.length > MAX_PAYLOAD)
			{
				throw new IOException("The payload exceeds " + MAX_PAYLOAD + " bytes.");
			}
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}

	private static String detail(RuntimeException e, String message)
	{
		return Application.get().usesDevelopmentConfig() ? message + " " + e : message;
	}
}
