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

/**
 * Data stores are used to persist (read & write) Wicket page data to a persistent store like e.g.
 * files or databases.
 * 
 * @see IPageStore
 */
public interface IDataStore
{
	/**
	 * Get data associated with the session id and page id.
	 * 
	 * @param sessionId
	 *            Session ID
	 * @param id
	 *            Page ID
	 * @return All the page data persisted
	 */
	byte[] getData(String sessionId, int id);

	/**
	 * Remove all persisted data related to the session id and page id
	 * 
	 * @param sessionId
	 *            Session ID
	 * @param id
	 *            Page ID
	 */
	void removeData(String sessionId, int id);

	/**
	 * Remove all page data for the session id
	 * 
	 * @param sessionId
	 *            Session ID
	 */
	void removeData(String sessionId);

	/**
	 * Store the page data
	 * 
	 * @param sessionId
	 *            Session ID
	 * @param id
	 *            Page ID
	 * @param data
	 *            Page data
	 */
	void storeData(String sessionId, int id, byte[] data);

	/**
	 * Properly close the data store and possibly open resource handles
	 */
	void destroy();

	/**
	 * 
	 * @return whether the data store is replicated
	 */
	boolean isReplicated();

	/**
	 * @return whether the implementation can be wrapped in {@link AsynchronousDataStore}
	 */
	boolean canBeAsynchronous();
}
