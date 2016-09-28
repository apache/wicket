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
package org.apache.wicket.request.resource.caching.version;

import java.util.regex.Pattern;

import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.lang.Args;

/**
 * provides a static version string for all package resources 
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public class StaticResourceVersion implements IResourceVersion
{
	private final String version;
	private final Pattern pattern;

	/**
	 * create static version provider
	 *
	 * @param version
	 *             static version string to deliver for all queries resources
	 */
	public StaticResourceVersion(String version)
	{
		this.version = Args.notNull(version, "version");
		this.pattern = Pattern.compile(Pattern.quote(version));
	}

	@Override
	public String getVersion(IStaticCacheableResource resource)
	{
		return version;
	}

	@Override
	public Pattern getVersionPattern()
	{
		return pattern;
	}
}
