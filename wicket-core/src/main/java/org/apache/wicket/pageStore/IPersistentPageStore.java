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
 */
public interface IPersistentPageStore extends IPageStore
{

	/**
	 * Get the identifier for pages stored for the given context.
	 */
	String getContextIdentifier(IPageContext context);

	/**
	 * Get the identifiers for all pages stored in all contexts.
	 */
	Set<String> getContextIdentifiers();

	/**
	 * Get information about all persisted pages with the given context identifier.
	 */
	List<IPersistedPage> getPersistentPages(String contextIdentifier);

	/**
	 * Get total size of all pages stored in all contexts. 
	 *  
	 * @return
	 */
	Bytes getTotalSize();
}
