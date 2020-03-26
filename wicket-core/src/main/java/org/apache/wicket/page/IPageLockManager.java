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

import java.io.Serializable;

/**
 * Lock manager for {@link PageAccessSynchronizer} responsible for locking and unlocking pages for
 * the duration of a request.
 */
public interface IPageLockManager extends Serializable
{

	/**
	 * Acquire a lock to a page
	 *
	 * @param pageId
	 *            page id
	 * @throws CouldNotLockPageException
	 *             if lock could not be acquired
	 */
	void lockPage(int pageId) throws CouldNotLockPageException;

	/**
	 * Unlocks all pages locked by this thread
	 */
	void unlockAllPages();

	/**
	 * Unlocks a single page locked by the current thread.
	 *
	 * @param pageId
	 *            the id of the page which should be unlocked.
	 */
	void unlockPage(int pageId);

}
