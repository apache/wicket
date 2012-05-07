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

import java.io.Serializable;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * static resource which does not change for the lifetime of the application and should be
 * considered a candidate for long-term caching.
 * 
 * @author Peter Ertl
 * @since 1.5
 */
public interface IStaticCacheableResource extends IResource
{
	/**
	 * controls whether caching of the resource is enabled or disabled
	 * 
	 * @return @{code true} if caching is enabled
	 */
	boolean isCachingEnabled();
	
	/**
	 * get unique caching key for the resource stream produced by
	 * {@link #getCacheableResourceStream()}
	 * 
	 * @return serializable key with properly supports {@link #equals(Object)} and
	 *         {@link #hashCode()}
	 */
	Serializable getCacheKey();

	/**
	 * get static resource stream which will be unique to the related caching key
	 * {@link #getCacheKey()}
	 * 
	 * @return stream or <code>null</code> if no stream could be found
	 */
	IResourceStream getCacheableResourceStream();
}
