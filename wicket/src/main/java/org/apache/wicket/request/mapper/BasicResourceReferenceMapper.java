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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.time.Time;

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
	// timestamp cache stored in request cycle meta data
	protected static final MetaDataKey<Map<ResourceReference, Time>> TIMESTAMP_KEY = new MetaDataKey<Map<ResourceReference, Time>>()
	{
		private static final long serialVersionUID = 1L;
	};

	protected static final String TIMESTAMP_PREFIX = "-ts";

	private final IPageParametersEncoder pageParametersEncoder;

	// if true, timestamps should be added to resource names
	private final IProvider<Boolean> timestamps;

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 * @param timestamps
	 */
	public BasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder,
		IProvider<Boolean> timestamps)
	{
		this.pageParametersEncoder = pageParametersEncoder;
		this.timestamps = timestamps;
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
			int segmentsSize = url.getSegments().size();
			for (int i = 3; i < segmentsSize; ++i)
			{
				String segment = url.getSegments().get(i);

				// if timestamps are enabled the last segment (=resource name)
				// should be stripped of timestamps
				if (isTimestampsEnabled() && i + 1 == segmentsSize)
				{
					// The last segment eventually contains a timestamp which we have to remove
					// resource lookup will not care about timestamp but always deliver the
					// most current version of the resource. After all this whole timestamp
					// thing is about caching on proxies and browsers but does not affect wicket.
					segment = stripTimestampFromResourceName(segment);
				}
				if (name.length() > 0)
				{
					name.append("/");
				}
				name.append(segment);
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
						attributes.getStyle(), attributes.getVariation(), true, true);

				if (res != null)
				{
					return new ResourceReferenceRequestHandler(res, pageParameters);
				}
			}
		}
		return null;
	}

	private boolean isTimestampsEnabled()
	{
		return timestamps.get();
	}

	/**
	 * strip timestamp information from resource name
	 * 
	 * @param resourceName
	 * @return
	 */

	private String stripTimestampFromResourceName(final String resourceName)
	{
		int pos = resourceName.lastIndexOf('.');

		final String fullname = pos == -1 ? resourceName : resourceName.substring(0, pos);
		final String extension = pos == -1 ? null : resourceName.substring(pos);

		pos = fullname.lastIndexOf(TIMESTAMP_PREFIX);

		if (pos != -1)
		{
			final String timestamp = fullname.substring(pos + TIMESTAMP_PREFIX.length());
			final String basename = fullname.substring(0, pos);

			try
			{
				Long.parseLong(timestamp); // just check the timestamp is numeric

				// create filename without timestamp for resource lookup
				return extension == null ? basename : basename + extension;
			}
			catch (NumberFormatException e)
			{
				// some strange case of coincidence where the filename contains the timestamp prefix
				// but the timestamp itself is non-numeric - we interpret this situation as
				// "file has no timestamp"

			}
		}
		return resourceName;
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
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	public Url mapHandler(IRequestHandler requestHandler)
	{
		if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler referenceRequestHandler = (ResourceReferenceRequestHandler)requestHandler;
			ResourceReference reference = referenceRequestHandler.getResourceReference();

			Url url = new Url();

			List<String> segments = url.getSegments();
			segments.add(getContext().getNamespace());
			segments.add(getContext().getResourceIdentifier());
			segments.add(getClassName(reference.getScope()));

			StringTokenizer tokens = new StringTokenizer(reference.getName(), "/");

			while (tokens.hasMoreTokens())
			{
				String token = tokens.nextToken();

				// on the last component of the resource path add the timestamp
				if (isTimestampsEnabled() && tokens.hasMoreTokens() == false)
				{
					// get last modification of resource (cached during the current request cycle)
					Time lastModified = getLastModifiedTimestampUsingCache(reference);

					// if resource provides a timestamp we include it in resource name
					if (lastModified != null)
					{
						// check if resource name has extension
						int extensionAt = token.lastIndexOf('.');

						// create timestamped version of filename:
						//
						// filename :=
// [basename][timestamp-prefix][last-modified-milliseconds](.extension)
						//
						StringBuilder filename = new StringBuilder();
						filename.append(extensionAt == -1 ? token : token.substring(0, extensionAt));
						filename.append(TIMESTAMP_PREFIX);
						filename.append(lastModified.getMilliseconds());

						if (extensionAt != -1)
							filename.append(token.substring(extensionAt));

						token = filename.toString();
					}
				}
				segments.add(token);
			}

			encodeResourceReferenceAttributes(url, reference);
			PageParameters parameters = referenceRequestHandler.getPageParameters();
			if (parameters != null)
			{
				parameters = new PageParameters(parameters);
				// need to remove indexed parameters otherwise the URL won't be able to decode
				parameters.clearIndexed();
				url = encodePageParameters(url, parameters, pageParametersEncoder);
			}

			return url;
		}
		return null;
	}

	/**
	 * That method gets the last modification timestamp from the specified resource reference.
	 * <p/>
	 * The timestamp is cached in the meta data of the current request cycle to eliminate repeated
	 * lookups of the same resource reference which will harm performance.
	 * 
	 * @param resourceReference
	 *            resource reference
	 * 
	 * @return last modification timestamp or <code>null</code> if no timestamp provided
	 */
	protected Time getLastModifiedTimestampUsingCache(ResourceReference resourceReference)
	{
		// try to lookup current request cycle
		RequestCycle requestCycle = ThreadContext.getRequestCycle();

		// no request cycle: this should not happen unless we e.g. run a plain test case without
// WicketTester
		if (requestCycle == null)
			return resourceReference.getLastModified();

		// retrieve cache from current request cycle
		Map<ResourceReference, Time> cache = requestCycle.getMetaData(TIMESTAMP_KEY);

		// create it on first call
		if (cache == null)
		{
			cache = new HashMap<ResourceReference, Time>();
			requestCycle.setMetaData(TIMESTAMP_KEY, cache);
		}

		final Time lastModified;

		// lookup timestamp from cache (may contain NULL values which are valid)
		if (cache.containsKey(resourceReference))
		{
			lastModified = cache.get(resourceReference);
		}
		else
		{
			// otherwise retrieve timestamp from resource
			lastModified = resourceReference.getLastModified();

			// and put it in cache
			cache.put(resourceReference, lastModified);
		}
		return lastModified;
	}

	/**
	 * Remove a timestamp cache entry for a resource reference from the <b>current</b> request cycle
	 * <p/>
	 * Can't even imagine a valid situation but in case someone really needs it the method is there!
	 */
	public static void removeLastModifiedTimestampFromCache(ResourceReference resourceReference)
	{
		Args.notNull(resourceReference, "resourceReference");

		// lookup current request cycle
		RequestCycle cycle = RequestCycle.get();

		if (cycle != null)
		{
			// retrieve cache
			Map<ResourceReference, Time> cache = cycle.getMetaData(TIMESTAMP_KEY);

			// if there is a cache
			if (cache != null)
			{
				// remove the resource entry
				cache.remove(resourceReference);
			}
		}
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
