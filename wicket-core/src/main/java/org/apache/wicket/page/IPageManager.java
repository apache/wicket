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


/**
 * Page manager.
 * 
 * @author Matej Knopp
 */
public interface IPageManager
{
	/**
	 * 
	 * @return the page manager context
	 */
	IPageManagerContext getContext();

	/**
	 * Retrieve page instance with given id.
	 * 
	 * @param id
	 *      the id of the page to load
	 * @return page instance or <code>null</code>
	 * @throws CouldNotLockPageException if the page is already locked by another thread
	 * and the lock cannot be acquired for some timeout
	 */
	public IManageablePage getPage(int id) throws CouldNotLockPageException;

	/**
	 * Marks page as changed.
	 * <p><strong>Note:</strong>Only stateful pages are stored.</p>
	 * 
	 * @param page
	 *      the page that should be stored in the page stores at the end of the request.
	 * @throws CouldNotLockPageException if the page is already locked by another thread
	 * and the lock cannot be acquired for some timeout
	 */
	public void touchPage(IManageablePage page) throws CouldNotLockPageException;

	/**
	 * Marks page as non-changed.
	 * Could be used in Ajax requests to avoid storing the page if no changes have happened.
	 *
	 * @param page
	 *      the page that should <strong>not</strong> be stored in the page stores at the end of the request.
	 * @throws CouldNotLockPageException if the page is already locked by another thread
	 * and the lock cannot be acquired for some timeout
	 */
	void untouchPage(IManageablePage page);

	/**
	 * Returns whether this manager supports versioning. Managers that support versioning must store
	 * page snapshots.
	 * 
	 * @return whether this page manager supports versioning
	 */
	public boolean supportsVersioning();

	/**
	 * Commits the changes to external storage if the manager uses it.
	 * 
	 * Should also detach all pages that were touched during this request.
	 */
	public void commitRequest();

	/**
	 * Invoked when new session has been created.
	 */
	public void newSessionCreated();

	/**
	 * Invoked when the session has been expired.
	 * 
	 * @param sessionId
	 *      the id of the expired session
	 */
	public void sessionExpired(String sessionId);

	/**
	 * Destroy the page manager.
	 */
	public void destroy();
}
