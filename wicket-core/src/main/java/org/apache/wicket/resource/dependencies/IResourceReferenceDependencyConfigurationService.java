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
package org.apache.wicket.resource.dependencies;

import org.apache.wicket.request.resource.ResourceReference;

/**
 * You must implement this in order to use ResourceReferenceDependencyInjectingHeaderResponse. It is
 * passed into the ResourceReferenceDependencyInjectingHeaderResponse constructor, and that header
 * response uses it to determine dependencies for all resources.
 * 
 * @see ResourceReferenceDependencyInjectingHeaderResponse
 * @author Jeremy Thomerson
 */
public interface IResourceReferenceDependencyConfigurationService
{

	/**
	 * Takes a resource reference and returns a reference that is configured with the entire
	 * dependency tree for this reference populated.
	 * 
	 * Note: this method can not return null for any reference, even those that are not configured
	 * within it. That means that it may have the resource reference for wicket-ajax.js and other
	 * wicket-related (or other library's) references passed in, and it must turn those into an
	 * {@link AbstractResourceDependentResourceReference}, even if the dependencies of that
	 * reference are empty. This ensures that later IHeaderResponse's in your decoration chain can
	 * always rely on receiving the proper type of resource reference.
	 * 
	 * @param reference
	 *            the reference that you need to look up dependencies for
	 * @return the reference that has the entire dependency tree (all depths) populated
	 */
	AbstractResourceDependentResourceReference configure(ResourceReference reference);
}
