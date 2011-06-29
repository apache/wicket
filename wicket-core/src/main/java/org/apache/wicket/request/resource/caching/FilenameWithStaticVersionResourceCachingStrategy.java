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
import org.apache.wicket.util.lang.Args;

/**
 * resource caching strategy that adds a static version string to all resources. This
 * is a recommended solution if your deployment environment does not support stable timestamps
 * as used by {@link FilenameWithStaticVersionResourceCachingStrategy}. 
 * This is for example the case in some cluster environments.
 * When deploying an updated version of the application the version string should be 
 * changed. The version string could be hardcoded, taken from some build-tool generated 
 * properties file, injected by spring, etc.
 * 
 * @author Peter Ertl
 */
public class FilenameWithStaticVersionResourceCachingStrategy 
	extends AbstractFilenameWithVersionResourceCachingStrategy
{
	private static final String DEFAULT_VERSION_SUFFIX = "-ver_"; // 'ver' = version
	
	private final String version;

	public FilenameWithStaticVersionResourceCachingStrategy(String version)
	{
		this(DEFAULT_VERSION_SUFFIX, version);
	}

	public FilenameWithStaticVersionResourceCachingStrategy(String versionSuffix, String version)
	{
		super(versionSuffix);
		this.version = Args.notEmpty(version, "version");
	}

	@Override
	protected String getVersionStringForResource(ResourceReference reference)
	{
		return version;
	}
}
