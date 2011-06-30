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
package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.util.lang.Args;

/**
 * resource caching strategy that adds a last-modified timestamp to the query string of the resource
 * (this is similar to how wicket 1.4 does it when enabling timestamps on resources).
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class QueryStringWithVersionResourceCachingStrategy implements IResourceCachingStrategy
{
	private static final String DEFAULT_VERSION_PARAMETER = "ver";

	/**
	 * query string parameter name that contains the version string for the resource
	 */
	private final String versionParameter;

	/**
	 * resource version provider
	 */
	private final IResourceVersion resourceVersion;

	/**
	 * Constructor
	 * 
	 * @param resourceVersion
	 *                resource version provider
	 */
	public QueryStringWithVersionResourceCachingStrategy(IResourceVersion resourceVersion)
	{
		this(DEFAULT_VERSION_PARAMETER, resourceVersion);
	}

	/**
	 * Constructor
	 *
	 * @param versionParameter
	 *            name of timestamp parameter which will be added to query string
	 *            and contain the resource version string
	 * @param resourceVersion
	 *                resource version provider
	 */
	public QueryStringWithVersionResourceCachingStrategy(String versionParameter, 
	                                                     IResourceVersion resourceVersion)
	{
		this.versionParameter = Args.notEmpty(versionParameter, "timestampParameter");
		this.resourceVersion = Args.notNull(resourceVersion, "resourceVersion");
	}

	/**
	 * @return name of timestamp parameter which will be added to query string
	 */
	public final String getVersionParameter()
	{
		return versionParameter;
	}

	public void decorateUrl(ResourceUrl url, final PackageResourceReference reference)
	{
		String version = resourceVersion.getVersion(reference);

		if (version != null)
		{
			url.getParameters().set(versionParameter, version);
		}
	}

	public void undecorateUrl(ResourceUrl url)
	{
		final INamedParameters parameters = url.getParameters();
		
		if (parameters != null)
		{
			parameters.remove(versionParameter);
		}
	}

	public void decorateResponse(AbstractResource.ResourceResponse response)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
