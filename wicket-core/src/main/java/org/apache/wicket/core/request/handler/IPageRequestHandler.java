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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.request.component.IRequestablePage;

/**
 * Request handler that works with a page instance.
 *
 * @author Matej Knopp
 */
public interface IPageRequestHandler extends IPageClassRequestHandler
{
	/**
	 * Returns the page. Be aware that the page can be instantiated if this wasn't the case already.
	 *
	 * @return page instance
	 */
	IRequestablePage getPage();

	/**
	 * Returns the page id.
	 *
	 * @return page id
	 */
	Integer getPageId();

	/**
	 * Checks if the page instance is already created or if it will be created when
	 * {@link #getPage()} is called
	 * 
	 * @return {@code true} iff page instance is already created
	 */
	boolean isPageInstanceCreated();

	/**
	 * Returns the number of times this page has been rendered.
	 *
	 * @return the number of times this page has been rendered.
	 * @see IRequestablePage#getRenderCount()
	 */
	Integer getRenderCount();
}
