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

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.caching.version.IResourceVersion;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resource caching strategy that adds a version for the 
 * requested resource to the filename.
 * <p/>
 * versioned_filename := [basename][version-prefix][version](.extension)
 * <p/>
 * the <code>version</code> must not contain the <code>version-prefix</code> so
 * please use an unambiguous value for the <code>version-prefix</code>. The default
 * <code>version-prefix</code> is <code>{@value #DEFAULT_VERSION_PREFIX}</code>.
 * <p/> 
 * Since browsers and proxies use the versioned filename of the resource 
 * as a cache key a change to the version will also change the filename and 
 * cause a reliable cache miss. This enables us to set the caching duration
 * of the resource to a maximum and get best network performance.
 * <p/>
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class FilenameWithVersionResourceCachingStrategy implements IResourceCachingStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(FilenameWithVersionResourceCachingStrategy.class);

	private static final String DEFAULT_VERSION_PREFIX = "-ver-";
	
	/** string that marks the beginning the of the version in the decorated filename */
	private final String versionPrefix;

	/** resource version provider */
	private final IResourceVersion resourceVersion;

	/**
	 * create filename caching strategy with given version provider and 
	 * <code>version-prefix = '{@value #DEFAULT_VERSION_PREFIX}'</code>
	 * 
	 * @param resourceVersion
	 *            version provider
	 *            
	 * @see #FilenameWithVersionResourceCachingStrategy(String, org.apache.wicket.request.resource.caching.version.IResourceVersion) 
	 */
	public FilenameWithVersionResourceCachingStrategy(IResourceVersion resourceVersion)
	{
		this(DEFAULT_VERSION_PREFIX, resourceVersion);
	}

	/**
	 * Constructor
	 * 
	 * @param versionPrefix
	 *            string that marks the beginning the of the version in the decorated filename 
	 * @param resourceVersion
	 *            resource version object
	 * 
	 * @see #FilenameWithVersionResourceCachingStrategy(org.apache.wicket.request.resource.caching.version.IResourceVersion) 
	 */
	public FilenameWithVersionResourceCachingStrategy(String versionPrefix,
	                                                  IResourceVersion resourceVersion)
	{
		this.resourceVersion = Args.notNull(resourceVersion, "resourceVersion");
		this.versionPrefix = Args.notEmpty(versionPrefix, "versionPrefix");
	}

	/**
	 * @return string appended to the filename before the version string
	 */
	public final String getVersionPrefix()
	{
		return versionPrefix;
	}

	@Override
	public void decorateUrl(ResourceUrl url, IStaticCacheableResource resource)
	{
		// get version string for requested resource
		final String version = this.resourceVersion.getVersion(resource);

		// ignore resource if no version information is available
		if (version == null)
		{
			return;
		}

		// get undecorated filename
		final String filename = url.getFileName();

		if (filename.contains(getVersionPrefix()))
		{
			LOG.error("A resource with name '{}' contains the version prefix '{}' so the un-decoration will not work." +
					" Either use a different version prefix or rename this resource.", filename, getVersionPrefix());
		}

		// check if resource name has extension
		final int extensionAt = filename.lastIndexOf('.');

		// create filename with version:
		//
		// filename :=
		// [basename][version-prefix][version](.extension)
		//
		final StringBuilder versionedFilename = new StringBuilder();
		
		// add filename
		if (extensionAt == -1)
		{
			versionedFilename.append(filename);
		}
		else
		{
			versionedFilename.append(filename.substring(0, extensionAt));
		}
		// add version suffix
		versionedFilename.append(versionPrefix);
		
		// add version
		versionedFilename.append(version);

		// add extension if present
		if (extensionAt != -1)
		{
			versionedFilename.append(filename.substring(extensionAt));
		}
		// set versioned filename
		url.setFileName(versionedFilename.toString());
	}

	@Override
	public void undecorateUrl(ResourceUrl url)
	{
		final String filename = url.getFileName();
		
		// check for extension
		int pos = filename.lastIndexOf('.');

		// get name of file without extension (but with version string)
		final String fullname = pos == -1 ? filename : filename.substring(0, pos);
		
		// get extension of file if present
		final String extension = pos == -1 ? null : filename.substring(pos);

		// get position of version string
		pos = fullname.lastIndexOf(versionPrefix);

		// remove version string if it exists
		if (pos != -1)
		{
			// get filename before version string
			final String basename = fullname.substring(0, pos);

			// create filename without version string 
			// (required for working resource lookup)
			url.setFileName(extension == null? basename : basename + extension);

			// store the version in the request cycle
			RequestCycle requestCycle = RequestCycle.get();
			if (requestCycle != null)
			{
				int idx = fullname.indexOf(versionPrefix);
				String urlVersion = fullname.substring(idx + versionPrefix.length());
				requestCycle.setMetaData(URL_VERSION, urlVersion);
			}
		}
	}

	/**
	 * set resource caching to maximum and set cache-visibility to 'public'
	 * 
	 * @param response
	 */
	@Override
	public void decorateResponse(AbstractResource.ResourceResponse response, IStaticCacheableResource resource)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
