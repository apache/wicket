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
package org.apache.wicket.resource.bundles;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceBundles;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.ResourceReference;

/***
 * Interface implemented by {@link ResourceReference}s that are bundles. A bundle is a resource that
 * combines several resources in one item, for example, several javascript files combined into one.
 * Bundles are rendered as a single {@link HeaderItem}, rather than several, and require only one
 * HTTP request to download, which can greatly reduce the number HTTP requests needed to get all
 * resources for a page. Resources need to be registered in {@link ResourceBundles} under
 * {@link Application#getResourceBundles()}.
 * 
 * @author papegaaij
 */
public interface IResourceBundle
{
	/**
	 * @return the resources that are provided (part of) this resource.
	 */
	Iterable<? extends HeaderItem> getProvidedResources();
}
