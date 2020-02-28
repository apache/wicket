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
package org.apache.wicket.page;

import org.apache.wicket.Page;
import org.apache.wicket.pageStore.IPageStore;

/**
 * A manager of pages - facade between {@link Page}s and {@link IPageStore}s they are stored in.
 * 
 * @see PageManager
 */
public interface IPageManager
{
	/**
	 * Is versionining of pages supported, see {@link IPageStore#supportsVersioning()}.
	 * 
	 * @return {@code true} if versioning is supported
	 */
	boolean supportsVersioning();

	/**
	 * Get a page
	 * 
	 * @param pageId
	 *            id of page
	 * @return page, may be <code>null</code>
	 */
	IManageablePage getPage(int pageId);

	/**
	 * Remove a page
	 * 
	 * @param page
	 *            page to remove
	 */
	void removePage(IManageablePage page);

	/**
	 * Add a page.
	 * 
	 * @param page
	 *            page to add
	 */
	void touchPage(IManageablePage page);
	
	/**
	 * Marks page as non-changed.
	 * Could be used in Ajax requests to avoid storing the page if no changes have happened.
	 *
	 * @param page
	 *      the page that should <strong>not</strong> be stored in the page stores at the end of the request.
	 */
	void untouchPage(IManageablePage page);

	/**
	 * Clear all pages.
	 */
	void clear();

	/**
	 * Detach at end of request.
	 */
	void detach();

	/**
	 * Destroy when application is destroyed.
	 */
	void destroy();

	/**
	 * Get the storage of pages, optional.
	 *  
	 * @return store or <code>null</code>
	 */
	IPageStore getPageStore();
}
