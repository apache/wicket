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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * resource caching strategy that adds a last-modified
 * timestamp to the filename
 * <p/>
 * timestamped_filename :=
 * [basename][timestamp-prefix][last-modified-milliseconds](.extension)
 *
 * Normally the resource names won't change when the resource ifself changes, for example when
 * you add a new style to your CSS sheet. This can be very annoying as browsers (and proxies)
 * usally cache resources in their cache based on the filename and therefore won't update.
 * Unless you change the file name of the resource, force a reload or clear the browser's cache
 * the page will still render with your old CSS.
 * <p/>
 * Depending on HTTP response headers like 'Last-Modified' and 'Cache' automatic cache
 * invalidation can take very, very long or neven happen at all.
 * <p/>
 * Enabling timestamps on resources with this strategy will inject the last modification time of the
 * resource into the filename (the name will look something like 'style-ts1282915831000.css' where the large
 * number is the last modified date in milliseconds and '-ts' is a prefix to avoid conflicts
 * with filenames that already contain a number before their extension. *
 * <p/>
 * Since browsers and proxies use the filename of the resource as a cache key the changed
 * filename will not hit the cache and the page gets rendered with the changed file.
 * <p/>
 *
 * @author Peter Ertl
 */
public class FilenameWithTimestampResourceCachingStrategy extends AbstractResourceCachingStrategy
{
	protected static final String DEFAULT_TIMESTAMP_PREFIX = "-ts";

	private String timestampPrefix;

	public FilenameWithTimestampResourceCachingStrategy()
	{
		timestampPrefix = DEFAULT_TIMESTAMP_PREFIX;
	}

	public String getTimestampPrefix()
	{
		return timestampPrefix;
	}

	public void setTimestampPrefix(String timestampPrefix)
	{
		Args.notEmpty(timestampPrefix, "timestampPrefix");
		this.timestampPrefix = timestampPrefix;
	}

	public String decorateRequest(String filename, PageParameters parameters, ResourceReference reference)
	{
		Time lastModified = getLastModified(reference);

		if (lastModified == null)
			return filename;

		// check if resource name has extension
		int extensionAt = filename.lastIndexOf('.');

		// create timestamped version of filename:
		//
		// filename :=
		// [basename][timestamp-prefix][last-modified-milliseconds](.extension)
		//
		StringBuilder timestampedFilename = new StringBuilder();
		timestampedFilename.append(extensionAt == -1 ? filename : filename.substring(0, extensionAt));
		timestampedFilename.append(timestampPrefix);
		timestampedFilename.append(lastModified.getMilliseconds());

		if (extensionAt != -1)
			timestampedFilename.append(filename.substring(extensionAt));

		return timestampedFilename.toString();
	}

	public String sanitizeRequest(String filename, PageParameters parameters)
	{
		int pos = filename.lastIndexOf('.');

		final String fullname = pos == -1 ? filename : filename.substring(0, pos);
		final String extension = pos == -1 ? null : filename.substring(pos);

		pos = fullname.lastIndexOf(timestampPrefix);

		if (pos != -1)
		{
			final String timestamp = fullname.substring(pos + timestampPrefix.length());
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
		return filename;
	}

	/**
	 * set resource caching to maximum and set cache-visibility to 'public'
	 *
	 * @param response
	 */
	public void processResponse(AbstractResource.ResourceResponse response)
	{
		response.setCacheDurationToMaximum();
		response.setCacheScope(WebResponse.CacheScope.PUBLIC);
	}
}
