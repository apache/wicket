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
package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;


/**
 * Represents object capable of providing a page instance. In some cases the implementation class
 * might now page class and page parameters without having the actual page instance. Thus it's
 * recommended to call {@link #getPageParameters()} instead of calling {@link #getPageInstance()}
 * .getPageParameters(). Same goes for page class.
 * 
 * @author Matej Knopp
 */
public interface IPageProvider
{
	/**
	 * Returns page instance specified by the constructor.
	 * 
	 * @return page instance
	 * @throws StalePageException
	 *             if render count has been specified in constructor and the render count of page
	 *             does not match the value
	 * @throw {@link PageExpiredException} if the specified page could not have been found and the
	 *        constructor used did not provide enough information to create new page instance
	 */
	IRequestablePage getPageInstance();

	/**
	 * Returns {@link PageParameters} of the page.
	 * 
	 * @return page parameters
	 */
	PageParameters getPageParameters();

	/**
	 * Returns whether calling getPageInstance() will result in creating new page instance or
	 * whether it will be an existing intance (even though it might be pulled from page store).
	 * 
	 * @return <code>true</code> if calling {@link #getPageInstance()} will create new page
	 *         instance, <code>false</code> otherwise.
	 */
	boolean isNewPageInstance();

	/**
	 * Returns class of the page.
	 * 
	 * @return page class
	 */
	Class<? extends IRequestablePage> getPageClass();

	/**
	 * Detaches the page if it has been loaded.
	 */
	void detach();
}