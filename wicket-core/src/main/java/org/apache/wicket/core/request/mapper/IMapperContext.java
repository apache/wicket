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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;

/**
 * Utility interface for providing and creating new page instances.
 *
 * @author Matej Knopp
 */
public interface IMapperContext extends IPageSource
{
	/**
	 * @return the namespace for Wicket URLs.
	 */
	String getNamespace();

	/**
	 * @return identifier for non bookmarkable URLs
	 */
	String getPageIdentifier();

	/**
	 * @return identifier for bookmarkable URLs
	 */
	String getBookmarkableIdentifier();

	/**
	 * @return identifier for resources
	 */
	String getResourceIdentifier();

	/**
	 * @return {@link ResourceReferenceRegistry}
	 */
	ResourceReferenceRegistry getResourceReferenceRegistry();

	/**
	 * Returns the listener interface name as string.
	 *
	 * @param listenerInterface
	 * @return listener interface name as string
	 */
	String requestListenerInterfaceToString(RequestListenerInterface listenerInterface);

	/**
	 * Returns listener interface for the name
	 *
	 * @param interfaceName
	 * @return listener interface
	 */
	RequestListenerInterface requestListenerInterfaceFromString(String interfaceName);

	/**
	 * Returns the home page class.
	 *
	 * @return home page class
	 */
	Class<? extends IRequestablePage> getHomePageClass();
}
