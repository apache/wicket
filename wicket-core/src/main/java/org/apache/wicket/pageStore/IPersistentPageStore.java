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

import java.util.List;
import java.util.Set;

import org.apache.wicket.util.lang.Bytes;

/**
 * A store that can provide information about stored pages.
 * <p>
 * This is an optional interface to a store that is not involved during normal page processing.
 * Rather, it is used for analysis of application memory footprint.
 */
public interface IPersistentPageStore extends IPageStore
{

	/**
	 * Get the session identifier for pages stored for the given context.
	 * 
	 * @param context
	 *            a context of pages
	 * @return the identifier of the session.
	 * 
	 * @see #getPersistedPages(String)
	 */
	String getSessionIdentifier(IPageContext context);

	/**
	 * Get the identifiers for all stored sessions.
	 * 
	 * @return the identifiers of all session.
	 */
	Set<String> getSessionIdentifiers();

	/**
	 * Get information about all persisted pages with the given session identifier.
	 * 
	 * @param the
	 *            identifier of the session.
	 * @return all persisted pages
	 */
	List<IPersistedPage> getPersistedPages(String sessionIdentifier);

	/**
	 * Get total size of all pages stored in all contexts.
	 * <p>
	 * Optional operation, may return <code>null</code>.
	 * 
	 * @return total size or <code>null</code>
	 */
	Bytes getTotalSize();
}
