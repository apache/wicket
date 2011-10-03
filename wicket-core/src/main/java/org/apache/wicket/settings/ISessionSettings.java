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
package org.apache.wicket.settings;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Session;

/**
 * Interface for session related settings
 * <p>
 * <i>pageFactory </i>- The factory class that is used for constructing page instances.
 * <p>
 * <i>pageMapEvictionStrategy </i>- The strategy for evicting pages from page maps when they are too
 * full
 * <p>
 * <i>maxPageMaps </i>- The maximum number of page maps allowed in a session (to prevent denial of
 * service attacks)
 * <p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface ISessionSettings
{
	/**
	 * Gets the factory to be used when creating pages
	 * 
	 * @return The default page factory
	 * @deprecated Use {@link Session#getPageFactory()}
	 */
	@Deprecated
	IPageFactory getPageFactory();

	/**
	 * Sets the factory to be used when creating pages.
	 * 
	 * @param pageFactory
	 *            The default factory
	 * @deprecated Use {@link Application#newPageFactory()} instead.
	 */
	@Deprecated
	void setPageFactory(final IPageFactory pageFactory);
}