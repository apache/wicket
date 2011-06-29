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
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * base resource caching strategy that adds a version string for the 
 * requested resource to the filename.
 * <p/>
 * versioned_filename := [basename][version-suffix][version](.extension)
 * <p/>
 * Since browsers and proxies use this versioned filename of the resource as a 
 * cache key a change to the version will cause a cache miss and subsequent reload.
 * <p/>
 * 
 * @author Peter Ertl
 */
public abstract class AbstractFilenameWithVersionResourceCachingStrategy 
	extends AbstractResourceCachingStrategy
{
	/** suffix that uniquely identifies beginning of the version 
	 * string inside the resource filename */
	private final String versionSuffix;

	/**
	 * Constructor
	 * 
	 * @param versionSuffix
	 *            string appended to the filename before the version string
	 */
	public AbstractFilenameWithVersionResourceCachingStrategy(String versionSuffix)
	{
		this.versionSuffix = Args.notEmpty(versionSuffix, "versionSuffix");
	}

	/**
	 * @return string appended to the filename before the version string
	 */
	public final String getVersionSuffix()
	{
		return versionSuffix;
	}

	public void decorateUrl(ResourceUrl url, ResourceReference reference)
	{
		// get version string for requested resource
		final String version = getVersionStringForResource(reference);
		
		// ignore resource if no version information is available
		if (version == null)
		{
			return;
		}

		// get undecorated filename
		final String filename = url.getFileName();

		// check if resource name has extension
		final int extensionAt = filename.lastIndexOf('.');

		// create filename with version:
		//
		// filename :=
		// [basename][version-suffix][version](.extension)
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
		versionedFilename.append(versionSuffix);
		
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
		pos = fullname.lastIndexOf(versionSuffix);

		// remove version string if it exists
		if (pos != -1)
		{
			// get filename before version string
			final String basename = fullname.substring(0, pos);

			// create filename without version string 
			// (required for working resource lookup)
			url.setFileName(extension == null? basename : basename + extension);
		}
	}

	/**
	 * get string that uniquely identifies the current version of the resource
	 * 
	 * @param reference
	 *          resource reference
	 * @return string that uniquely identifies the current version of the resource
	 */
	protected abstract String getVersionStringForResource(ResourceReference reference);

	/**
	 * set resource caching to maximum and set cache-visibility to 'public'
	 * 
	 * @param response
	 */
	public void decorateResponse(AbstractResource.ResourceResponse response)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
