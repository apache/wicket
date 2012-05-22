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
package org.apache.wicket.pageStore;

import java.io.Serializable;

import org.apache.wicket.page.IManageablePage;

/**
 * {@link IPageStore} role is to mediate the storing and loading of pages done by {@link IDataStore}
 * s. {@link IPageStore} may pre-process the pages before passing them to
 * {@link IDataStore#storeData(String, int, byte[])} and to post-process them after
 * {@link IDataStore#getData(String, int)}.
 * 
 * @see IDataStore
 */
public interface IPageStore
{
	/**
	 * Destroy the store.
	 */
	void destroy();

	/**
	 * Restores a page from the persistent layer.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param pageId
	 *            The id of the page.
	 * @return The page
	 */
	IManageablePage getPage(String sessionId, int pageId);

	/**
	 * Removes a page from the persistent layer.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param pageId
	 *            The id of the page.
	 */
	void removePage(String sessionId, int pageId);

	/**
	 * Stores the page to a persistent layer. The page should be stored under the id and the version
	 * number.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param page
	 *            The page to store
	 */
	void storePage(String sessionId, IManageablePage page);

	/**
	 * The page store should cleanup all the pages for that sessionid.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 */
	void unbind(String sessionId);

	/**
	 * Process the page before the it gets serialized. The page can be either real page instance or
	 * object returned by {@link #restoreAfterSerialization(Serializable)}.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param page
	 * @return The Page itself or a SerializedContainer for that page
	 */
	Serializable prepareForSerialization(String sessionId, Object page);

	/**
	 * This method should restore the serialized page to intermediate object that can be converted
	 * to real page instance using {@link #convertToPage(Object)}.
	 * 
	 * @param serializable
	 * @return Page
	 */
	Object restoreAfterSerialization(Serializable serializable);

	/**
	 * Converts a page representation to an instance of {@link IManageablePage}
	 * 
	 * @param page
	 *            some kind of page representation
	 * @return page
	 */
	IManageablePage convertToPage(Object page);
}
