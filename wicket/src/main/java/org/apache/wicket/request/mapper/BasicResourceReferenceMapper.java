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
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.WicketObjects;
import org.apache.wicket.util.time.Time;

import java.util.List;
import java.util.StringTokenizer;

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
	private static final String TIMESTAMP_PREFIX = "-ts";
	private final IPageParametersEncoder pageParametersEncoder;

	// if true, timestamps should be added to resource names
	private final IProvider<Boolean> timestamps;

	/**
	 * Construct.
	 *
	 * @param pageParametersEncoder
	 * @param relativePathPartEscapeSequence
	 */
	public BasicResourceReferenceMapper(IPageParametersEncoder pageParametersEncoder, IProvider<Boolean> timestamps)
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
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.org.apache.wicket.request.IRequestHandler)
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
					// get last modification of resource
					Time lastModified = reference.getLastModified();

					// if resource provides a timestamp we include it in resource name
					if (lastModified != null)
					{
						// check if resource name has extension
						int extensionAt = token.lastIndexOf('.');

						// create timestamped version of filename:
						//
						//   filename := [basename][timestamp-prefix][last-modified-milliseconds](.extension)
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
