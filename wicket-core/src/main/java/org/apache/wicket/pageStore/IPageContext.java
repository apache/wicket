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
import java.util.function.Supplier;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Context of a {@link IManageablePage} when it is stored in an {@link IPageStore}, decoupling it
 * from request cycle and session.
 * 
 * @author Matej Knopp
 * @author svenmeier
 */
public interface IPageContext
{
	/**
	 * Get data from the current request cycle.
	 * 
	 * @param key
	 *            key
	 * @param defaultValue
	 *            default value to use if not present
	 * 
	 * @see RequestCycle#getMetaData(MetaDataKey)
	 */
	<T> T getRequestData(MetaDataKey<T> key, Supplier<T> defaultValue);

	/**
	 * Get an attribute from the session. <br>
	 * Binds the session if not already set <em>and</em> supplier is not <code>null</code>.
	 * Sets the session attribute if supplier is not <code>null</code>.
	 * 
	 * @param key
	 *            key
	 * @param defaultValue
	 *            default value to use if not present
	 * @return value
	 * 
	 * @see Session#getAttribute(String)
	 */
	<T extends Serializable> T getSessionAttribute(String key, Supplier<T> defaultValue);

	/**
	 * Get metadata from the session. <br>
	 * Binds the session if not already set <em>and</em> supplier is not <code>null</code>.
	 * Sets the session attribute if supplier is not <code>null</code>.
	 *
	 * @param key
	 *            key
	 * @param defaultValue
	 *            optional supplier of a default value to use if not present
	 * @return value
	 * 
	 * @see Session#getMetaData(MetaDataKey)
	 */
	<T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> defaultValue);

	/**
	 * Get the identifier of the session.
	 * 
	 * @param bind
	 *            should the session be bound
	 * @return session id, might be <code>null</code> if not bound yet
	 */
	String getSessionId(boolean bind);
}