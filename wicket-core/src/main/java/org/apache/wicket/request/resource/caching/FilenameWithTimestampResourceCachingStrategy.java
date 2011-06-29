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

import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.time.Time;

/**
 * resource caching strategy that adds a last-modified timestamp to the filename
 * <p/>
 * timestamped_filename := [basename][timestamp-prefix][last-modified-milliseconds](.extension)
 *
 * Normally the resource names won't change when the resource ifself changes, for example when you
 * add a new style to your CSS sheet. This can be very annoying as browsers (and proxies) usally
 * cache resources in their cache based on the filename and therefore won't update. Unless you
 * change the file name of the resource, force a reload or clear the browser's cache the page will
 * still render with your old CSS.
 * <p/>
 * Depending on HTTP response headers like 'Last-Modified' and 'Cache' automatic cache invalidation
 * can take very, very long or neven happen at all.
 * <p/>
 * Enabling timestamps on resources with this strategy will inject the last modification time of the
 * resource into the filename (the name will look something like 'style-ts1282915831000.css' where
 * the large number is the last modified date in milliseconds and '-ts' is a prefix to avoid
 * conflicts with filenames that already contain a number before their extension. *
 * <p/>
 * Since browsers and proxies use the filename of the resource as a cache key the changed filename
 * will not hit the cache and the page gets rendered with the changed file.
 * <p/>
 *
 * @author Peter Ertl
 */
public class FilenameWithTimestampResourceCachingStrategy extends AbstractFilenameWithVersionResourceCachingStrategy
{
	private static final String DEFAULT_VERSION_SUFFIX = "-ts_"; // 'ts' = timestamp

	public FilenameWithTimestampResourceCachingStrategy()
	{
		super(DEFAULT_VERSION_SUFFIX);
	}

	public FilenameWithTimestampResourceCachingStrategy(String versionSuffix)
	{
		super(versionSuffix);
	}

	@Override
	protected String getVersionStringForResource(ResourceReference reference)
	{
		final Time lastModified = getLastModified(reference);

		if (lastModified == null)
		{
			return null;
		}
		return String.valueOf(lastModified.getMilliseconds());
	}
}
