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
package org.apache.wicket.authorization;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;

/**
 * Exception that is thrown when a request to a resource is not allowed.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class UnauthorizedResourceRequestException extends AuthorizationException
{
	private static final long serialVersionUID = 1L;

	private final IResource resource;

	private final PageParameters parameters;

	/**
	 * Construct.
	 * 
	 * @param resource
	 *            The unauthorized resource
	 * @param parameters
	 *            The request parameters
	 */
	public UnauthorizedResourceRequestException(final IResource resource, PageParameters parameters)
	{
		super("Not authorized to instantiate class " + resource.getClass().getName());

		this.resource = resource;
		this.parameters = parameters;
	}

	public IResource getResource()
	{
		return resource;
	}

	public PageParameters getParameters()
	{
		return parameters;
	}
}
