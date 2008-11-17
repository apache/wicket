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
package org.apache._wicket.request.encoder;

import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.encoder.parameters.PageParametersEncoder;
import org.apache._wicket.request.encoder.parameters.SimplePageParametersEncoder;
import org.apache._wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache._wicket.request.handler.resource.ResourceRequestHandler;
import org.apache._wicket.request.request.Request;
import org.apache._wicket.resource.Resource;
import org.apache._wicket.resource.ResourceReference;
import org.apache.wicket.util.lang.Classes;

/**
 * Generic {@link ResourceReference} encoder that encodes and decodes non-mounted
 * {@link ResourceReference}s.
 * 
 * @author Matej Knopp
 */
public class ResourceReferenceEncoder extends AbstractResourceReferenceEncoder
{
	private final PageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 */
	public ResourceReferenceEncoder(PageParametersEncoder pageParametersEncoder)
	{
		this.pageParametersEncoder = pageParametersEncoder;
	}

	/**
	 * Construct.
	 */
	public ResourceReferenceEncoder()
	{
		this(new SimplePageParametersEncoder());
	}

	protected EncoderContext getContext()
	{
		return null;
	};

	public RequestHandler decode(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() >= 4 &&
			urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()))
		{
			String className = url.getSegments().get(2);
			StringBuilder name = new StringBuilder();
			for (int i = 3; i < url.getSegments().size(); ++i)
			{
				if (name.length() > 0)
				{
					name.append("/");
				}
				name.append(url.getSegments().get(i));
			}

			ResourceReferenceAttributes attributes = getResourceReferenceAttributes(url);

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(url,
				request.getRequestParameters(), url.getSegments().size(), pageParametersEncoder);

			Class<?> scope = resolveClass(className);
			if (scope != null)
			{
				ResourceReference res = getContext().getResourceReferenceRegistry()
					.getResourceReference(scope, name.toString(), attributes.locale,
						attributes.style, false);
				if (res != null)
				{
					Resource resource = res.getResource();
					if (resource != null)
					{
						ResourceRequestHandler handler = new ResourceRequestHandler(resource,
							attributes.locale, attributes.style, pageParameters);
						return handler;
					}
				}
			}
		}
		return null;
	}

	protected Class<?> resolveClass(String name)
	{
		return Classes.resolveClass(name);
	}

	protected String getClassName(Class<?> scope)
	{
		return scope.getName();
	}

	public Url encode(RequestHandler requestHandler)
	{
		if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
			ResourceReference reference = referenceRequestHandler.getResourceReference();
			Url url = new Url();
			url.getSegments().add(getContext().getNamespace());
			url.getSegments().add(getContext().getResourceIdentifier());
			url.getSegments().add(getClassName(reference.getScope()));
			String nameParts[] = reference.getName().split("/");
			for (String name : nameParts)
			{
				url.getSegments().add(name);
			}
			encodeResourceReferenceAttributes(url, reference);
			PageParameters parameters = referenceRequestHandler.getPageParameters();
			if (parameters != null)
			{
				parameters = new PageParameters(parameters);
				// need to remove indexed parameters otherwise the URL won't be able to decode
				parameters.clearIndexedParameters();
				url = encodePageParameters(url, parameters, pageParametersEncoder);
			}
			return url;
		}
		return null;
	}

	public int getMachingSegmentsCount(Request request)
	{
		// always return 0 here so that the mounts have higher priority
		return 0;
	}

}
