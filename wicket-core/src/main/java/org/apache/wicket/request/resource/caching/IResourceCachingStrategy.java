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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * caching strategy for resources
 * <p/>
 * it can add and remove caching information to the filename
 * and query string parameters of the requested resource
 *
 * @author Peter Ertl
 */
public interface IResourceCachingStrategy
{
	/**
	 * add caching related information to filename + parameters
	 *
	 * @param filename
	 *            original filename without a timestamp
	 * @param parameters
	 *            request parameters (<b>you are only allowed to add named
	 *            parameters but not indexed parameters</b>)
	 * @param reference
	 *            resource reference
	 *
	 * @return modified filename caching related information
	 */
	String decorateRequest(String filename, PageParameters parameters, ResourceReference reference);

	/**
	 * removes caching related information from filename + parameters
	 *
	 *
	 * @param filename
	 *           original filename that eventually contains caching related information
	 * @param parameters
	 *           page parameters (must be sanitized of caching related parameters)
	 *
	 * @return sanitized filename without caching related information
	 */
	String sanitizeRequest(String filename, PageParameters parameters);

	/**
	 * decorate resource response
	 *
	 * @param response
	 */
	void processResponse(AbstractResource.ResourceResponse response);
}
