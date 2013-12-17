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

/**
 * Provides a version string for a cacheable resource specified by {@link org.apache.wicket.request.resource.caching.IStaticCacheableResource} 
 * <p/>
 * the version string will be used as a distinguishing mark when rendering resources
 * 
 * @see org.apache.wicket.request.resource.caching.QueryStringWithVersionResourceCachingStrategy
 * @see org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public interface IResourceVersion
{
	/**
	 * get unique string identifying the version of the resource
	 * 
	 * @param resource
	 *             cacheable resource
	 * @return unique version string or <code>null</code> 
	 *         if version string could not be calculated
	 */
	String getVersion(IStaticCacheableResource resource);
	
	/**
	 * a pattern that matches returned versions
	 * 
	 * @return a pattern or <code>null</code> if no pattern
	 *         is available
	 */
	Pattern getVersionPattern();
}
