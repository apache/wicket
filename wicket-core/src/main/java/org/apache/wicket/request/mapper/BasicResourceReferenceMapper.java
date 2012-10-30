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

import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.request.resource.MetaInfStaticResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Peter Ertl
 */
public class BasicResourceReferenceMapper extends AbstractResourceReferenceMapper
{
	private static final Logger log = LoggerFactory.getLogger(BasicResourceReferenceMapper.class);

	private final IPageParametersEncoder pageParametersEncoder;

	/** resource caching strategy */
	private final IProvider<? extends IResourceCachingStrategy> cachingStrategy;

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 * @param cachingStrategy
	 */
	public BasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
		IProvider<? extends IResourceCachingStrategy> cachingStrategy)
	{
		this.pageParametersEncoder = pageParametersEncoder;
		this.cachingStrategy = cachingStrategy;
	}

	public IRequestHandler mapRequest(Request request)
	{
		Url url = request.getUrl();

		// extract the PageParameters from URL if there are any
		PageParameters pageParameters = extractPageParameters(request, url.getSegments().size(),
			pageParametersEncoder);

		if (url.getSegments().size() >= 4 &&
			urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()))
		{
			String className = url.getSegments().get(2);
			StringBuilder name = new StringBuilder();
			int segmentsSize = url.getSegments().size();
			for (int i = 3; i < segmentsSize; ++i)
			{
				String segment = url.getSegments().get(i);

				// ignore invalid segments
				if (segment.contains("/"))
				{
					return null;
				}

				// remove caching information
				if (i + 1 == segmentsSize && Strings.isEmpty(segment) == false)
				{
					// The filename + parameters eventually contain caching
					// related information which needs to be removed
					ResourceUrl resourceUrl = new ResourceUrl(segment, pageParameters);
					getCachingStrategy().undecorateUrl(resourceUrl);
					segment = resourceUrl.getFileName();

					if (Strings.isEmpty(segment))
					{
						throw new IllegalStateException("caching strategy returned empty name for " + resourceUrl);
					}
				}
				if (name.length() > 0)
				{
					name.append("/");
				}
				name.append(segment);
			}

			ResourceReference.UrlAttributes attributes = getResourceReferenceAttributes(url);

			Class<?> scope = resolveClass(className);

			if (scope != null && scope.getPackage() != null)
			{
				ResourceReference res = getContext().getResourceReferenceRegistry()
					.getResourceReference(scope, name.toString(), attributes.getLocale(),
						attributes.getStyle(), attributes.getVariation(), true, true);

				if (res != null)
				{
					return new ResourceReferenceRequestHandler(res, pageParameters);
				}
			}
		}
		return null;
	}

	private IResourceCachingStrategy getCachingStrategy()
	{
		return cachingStrategy.get();
	}

	protected Class<?> resolveClass(String name)
	{
		return WicketObjects.resolveClass(name);
	}

	protected String getClassName(Class<?> scope)
	{
		return scope.getName();
	}

	public Url mapHandler(IRequestHandler requestHandler)
	{
		if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
			ResourceReference reference = referenceRequestHandler.getResourceReference();

			Url url;

			if (reference instanceof MetaInfStaticResourceReference)
			{
				url = ((MetaInfStaticResourceReference)reference).mapHandler(referenceRequestHandler);
				// if running on Servlet 3.0 engine url is not null
				if (url != null)
				{
					return url;
				}
				// otherwise it has to be served by the standard wicket way
			}

			if (reference.canBeRegistered())
			{
				ResourceReferenceRegistry resourceReferenceRegistry = getContext().getResourceReferenceRegistry();
				resourceReferenceRegistry.registerResourceReference(reference);
			}

			url = new Url();

			List<String> segments = url.getSegments();
			segments.add(getContext().getNamespace());
			segments.add(getContext().getResourceIdentifier());
			segments.add(getClassName(reference.getScope()));

			// setup resource parameters
			PageParameters parameters = referenceRequestHandler.getPageParameters();

			if (parameters == null)
			{
				parameters = new PageParameters();
			}
			else
			{
				parameters = new PageParameters(parameters);

				// need to remove indexed parameters otherwise the URL won't be able to decode
				parameters.clearIndexed();
			}
			encodeResourceReferenceAttributes(url, reference);

			StringTokenizer tokens = new StringTokenizer(reference.getName(), "/");

			while (tokens.hasMoreTokens())
			{
				String token = tokens.nextToken();

				// on the last component of the resource path
				if (tokens.hasMoreTokens() == false && Strings.isEmpty(token) == false)
				{
					final IResource resource = reference.getResource();

					// apply caching if required
					if (resource instanceof IStaticCacheableResource)
					{
						// add caching related information to filename + query parameters
						final IStaticCacheableResource cacheable = (IStaticCacheableResource)resource;
						final ResourceUrl resourceUrl = new ResourceUrl(token, parameters);
						getCachingStrategy().decorateUrl(resourceUrl, cacheable);
						token = resourceUrl.getFileName();

						if (Strings.isEmpty(token))
						{
							throw new IllegalStateException("caching strategy returned empty name for " + resource);
						}
					}
				}
				segments.add(token);
			}

			if (parameters.isEmpty() == false)
			{
				url = encodePageParameters(url, parameters, pageParametersEncoder);
			}

			return url;
		}
		return null;
	}

	public int getCompatibilityScore(Request request)
	{
		Url url = request.getUrl();

		int score = -1;
		if (url.getSegments().size() >= 4 &&
			urlStartsWith(url, getContext().getNamespace(), getContext().getResourceIdentifier()))
		{
			score = 1;
		}

		return score;
	}
}
