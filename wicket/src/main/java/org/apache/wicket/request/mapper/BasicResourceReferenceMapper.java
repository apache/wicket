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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.WicketObjects;

/**
 * Generic {@link ResourceReference} encoder that encodes and decodes non-mounted
 * {@link ResourceReference}s.
 * <p>
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *    /wicket/resource/org.apache.wicket.ResourceScope/name
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?en
 *    /wicket/resource/org.apache.wicket.ResourceScope/name?-style
 *    /wicket/resource/org.apache.wicket.ResourceScope/resource/name.xyz?en_EN-style
 * </pre>
 * 
 * @author Matej Knopp
 * @author igor.vaynberg
 */
class BasicResourceReferenceMapper extends AbstractResourceReferenceMapper
{
	private final IPageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 * @param relativePathPartEscapeSequence
	 */
	public BasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder)
	{
		this.pageParametersEncoder = pageParametersEncoder;
	}

	/**
	 * Construct.
	 */
	public BasicResourceReferenceMapper()
	{
		this(new PageParametersEncoder());
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	public IRequestHandler mapRequest(Request request)
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

			ResourceReference.UrlAttributes attributes = getResourceReferenceAttributes(url);

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(request,
				url.getSegments().size(), pageParametersEncoder);

			Class<?> scope = resolveClass(className);
			if (scope != null)
			{
				ResourceReference res = getContext().getResourceReferenceRegistry()
					.getResourceReference(scope, name.toString(), attributes.getLocale(),
						attributes.getStyle(), attributes.getVariation(), true);

				if (res != null)
				{
					return new ResourceReferenceRequestHandler(res, pageParameters);
				}
			}
		}
		return null;
	}

	protected Class<?> resolveClass(String name)
	{
		return WicketObjects.resolveClass(name);
	}

	protected String getClassName(Class<?> scope)
	{
		return scope.getName();
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler requestHandler)
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

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	public int getCompatibilityScore(Request request)
	{
		// always return 0 here so that the mounts have higher priority
		return 0;
	}
}
