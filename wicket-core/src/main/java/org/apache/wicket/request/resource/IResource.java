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
package org.apache.wicket.request.resource;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

/**
 * Resource is an object capable of writing output to response.
 * 
 * @author Matej Knopp
 */
public interface IResource extends IClusterable
{
	/**
	 * Attributes that are provided to resource in the {@link IResource#respond(Attributes)} method.
	 * Attributes are set by the {@link ResourceRequestHandler}.
	 * 
	 * @author Matej Knopp
	 */
	public static class Attributes
	{
		private final Request request;
		private final Response response;
		private final PageParameters parameters;

		/**
		 * Construct.
		 * 
		 * @param request
		 * 
		 * @param response
		 * @param parameters
		 */
		public Attributes(Request request, Response response, PageParameters parameters)
		{
			Args.notNull(request, "request");
			Args.notNull(response, "response");

			this.request = request;
			this.response = response;
			this.parameters = parameters;
		}

		/**
		 * Construct.
		 * 
		 * @param request
		 * @param response
		 */
		public Attributes(Request request, Response response)
		{
			this(request, response, null);
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
	}

	/**
	 * Renders this resource to response using the provided attributes.
	 * 
	 * @param attributes
	 */
	void respond(Attributes attributes);
}
