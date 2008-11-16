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
package org.apache._wicket.resource;

import java.io.Serializable;
import java.util.Locale;

import org.apache._wicket.PageParameters;
import org.apache._wicket.request.handler.resource.ResourceRequestHandler;
import org.apache._wicket.request.request.Request;
import org.apache._wicket.request.response.Response;

/**
 * Resource is an object capable of writing output to response.
 * 
 * @author Matej Knopp
 */
public interface Resource extends Serializable
{
	/**
	 * Attributes that are provided to resource in the {@link Resource#respond(Attributes)} method.
	 * Attributes are set by the {@link ResourceRequestHandler}.
	 * 
	 * @author Matej Knopp
	 */
	public static class Attributes
	{
		private final Request request;
		private final Response response;
		private final Locale locale;
		private final String style;
		private final PageParameters parameters;

		/**
		 * Construct.
		 * 
		 * @param request
		 * 
		 * @param response
		 * @param locale
		 * @param style
		 * @param parameters
		 */
		public Attributes(Request request, Response response, Locale locale, String style,
			PageParameters parameters)
		{
			if (request == null)
			{
				throw new IllegalArgumentException("Argument 'request' may not be null.");
			}
			if (response == null)
			{
				throw new IllegalArgumentException("Argument 'response' may not be null.");
			}
			if (locale == null)
			{
				throw new IllegalArgumentException("Argument 'locale' may not be null.");
			}
			this.request = request;
			this.response = response;
			this.locale = locale;
			this.style = style;
			this.parameters = parameters;
		}

		/**
		 * Returns current request.
		 * 
		 * @return current request
		 */
		public Request getRequest()
		{
			return request;
		}

		/**
		 * Returns current response. The resource must write output to the response.
		 * 
		 * @return response
		 */
		public Response getResponse()
		{
			return response;
		}

		/**
		 * Returns requested locale. The locale is never null.
		 * 
		 * @return locale
		 */
		public Locale getLocale()
		{
			return locale;
		}

		/**
		 * If specified returns requested style. The style is optional.
		 * 
		 * @return style or <code>null</code>
		 */
		public String getStyle()
		{
			return style;
		}

		/**
		 * Returns additional parameters extracted from the request. If resource is created mounted
		 * {@link ResourceReference}, this method returns all (indexed and query) parameters after
		 * the mount path. For non mounted {@link ResourceReference}s this method will only return
		 * the query parameters. For component specific resources the behavior depends on the
		 * component.
		 * 
		 * @return page parameters or <code>null</code>
		 */
		public PageParameters getParameters()
		{
			return parameters;
		}
	};

	/**
	 * Renders this resource to response using the provided attributes.
	 * 
	 * @param attributes
	 */
	public void respond(Attributes attributes);
}
