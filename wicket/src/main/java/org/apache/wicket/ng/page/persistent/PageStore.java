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
package org.apache.wicket.ng.page.persistent;

import java.io.Serializable;

import org.apache.wicket.ng.page.ManageablePage;

public interface PageStore
{
	/**
	 * Destroy the store.
	 */
	void destroy();

	/**
	 * Restores a page from the persistent layer.
	 * 
	 * @param sessionId
	 * @param id
	 * @return The page
	 */
	ManageablePage getPage(String sessionId, int id);

	/**
	 * Removes a page from the persistent layer.
	 * 
	 * @param sessionId
	 *            The session of the page that must be removed
	 * @param id
	 *            The id of the page.
	 */
	void removePage(String sessionId, int id);

	/**
	 * Stores the page to a persistent layer. The page should be stored under the id and the version
	 * number.
	 * 
	 * @param sessionId
	 * @param page
	 */
	void storePage(String sessionId, ManageablePage page);

	/**
	 * The pagestore should cleanup all the pages for that sessionid.
	 * 
	 * @param sessionId
	 */
	void unbind(String sessionId);

	/*
	 * Some PageStores might want to preprocess page before serialization. For example if the
	 * PageStore serializes page, it might cache the serialized data after the request. So when the
	 * pagemap gets serialized (for session replication) in the request thread, the pagestore can
	 * provide the already serialized data.
	 */

	/**
	 * Process the page before the it gets serialized. The page can be either real page instance or
	 * object returned by {@link #restoreAfterSerialization(Serializable)}.
	 * 
	 * @param sessionId
	 * @param page
	 * @return The Page itself or a SerializedContainer for that page
	 */
	public Serializable prepareForSerialization(String sessionId, Object page);

	/**
	 * This method should restore the serialized page to intermediate object that can be converted
	 * to real page instance using {@link #convertToPage(Object)}.
	 * 
	 * @param sessionId
	 * @param serializable
	 * @return Page
	 */
	public Object restoreAfterSerialization(Serializable serializable);

	/**
	 * 
	 * @param page
	 * @return page
	 */
	public ManageablePage convertToPage(Object page);
}
