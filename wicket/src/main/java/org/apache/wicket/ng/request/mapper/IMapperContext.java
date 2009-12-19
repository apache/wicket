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
package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.ng.resource.ResourceReferenceRegistry;

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
	public String getNamespace();
	
	/**
	 * @return identifier for non bookmarkable URLs
	 */
	public String getPageIdentifier();
	
	/**
	 * @return identifier for bookmarkable URLs
	 */
	public String getBookmarkableIdentifier();
	
	/**
	 * @return identifier for resources
	 */
	public String getResourceIdentifier();
	
	/**
	 * @return {@link ResourceReferenceRegistry}
	 */
	public ResourceReferenceRegistry getResourceReferenceRegistry();
	
	/**
	 * Returns the listener interface name as string.
	 * 
	 * @param listenerInterface
	 * @return listener interface name as string
	 */
	public String requestListenerInterfaceToString(RequestListenerInterface listenerInterface);
	
	/**
	 * Returns listener interface for the name
	 * 
	 * @param interfaceName
	 * @return listener interface
	 */
	public RequestListenerInterface requestListenerInterfaceFromString(String interfaceName);
	
	/**
	 * Returns the home page class.
	 * 
	 * @return home page class
	 */
	public Class<? extends IRequestablePage> getHomePageClass();
}
