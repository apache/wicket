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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.resource.AbstractResource;

/**
 * caching strategy for cacheable resources
 * <p/>
 * it can add and remove caching information to the filename and query 
 * string parameters of the requested resource to control caches in the
 * browser and on the internet. It also can set http response caching headers.
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public interface IResourceCachingStrategy
{

	/**
	 * A key used to store the extracted resource's version in
	 * {@linkplain #undecorateUrl(ResourceUrl)} into the request cycle
	 */
	final MetaDataKey<String> URL_VERSION = new MetaDataKey<String>()
	{
	};

	/**
	 * add caching related information to filename + parameters
	 * 
	 * @param url
	 *            parameters to which caching information should be added and which will be used to
	 *            construct the url to the resource
	 * 
	 * @param resource
	 *            cacheable resource
	 */
	void decorateUrl(ResourceUrl url, IStaticCacheableResource resource);

	/**
	 * Removes caching related information from filename + parameters. In essenese this method
	 * undoes what 
	 * {@link #decorateUrl(ResourceUrl, IStaticCacheableResource)} 
	 * did.
	 * 
	 * @param url
	 *            parameters that were used to construct the url to the resource and from which
	 *            previously added caching information should be stripped
	 */
	void undecorateUrl(ResourceUrl url);

	/**
	 * decorate resource response
	 * 
	 * @param response
	 */
	void decorateResponse(AbstractResource.ResourceResponse response, IStaticCacheableResource resource);

	/**
	 * Clears any stateful information
	 */
	void clearCache();

}
