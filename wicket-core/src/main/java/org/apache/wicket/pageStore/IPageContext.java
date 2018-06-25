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

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.page.IManageablePage;

/**
 * Context of a {@link IManageablePage} when it is store in a {@link IPageStore}, decoupling it from
 * request cycle and session.
 * 
 * @author Matej Knopp
 * @author svenmeier
 */
public interface IPageContext
{
	/**
	 * Set data into the current request.
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	<T> void setRequestData(MetaDataKey<T> key, T value);

	/**
	 * Get data from the current request.
	 * 
	 * @param key
	 *            key
	 * @return value
	 */
	<T> T getRequestData(MetaDataKey<T> key);

	/**
	 * Set an attribute in the session.
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	<T extends Serializable> void setSessionAttribute(String key, T value);

	/**
	 * Get an attribute from the session.
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	<T extends Serializable> T getSessionAttribute(String key);

	/**
	 * Set data into the session - only if it is not set already.
	 * <p>
	 * Recommended usage:
	 * <pre>
	 * <code>
	 * SessionData data = context.getSessionData(KEY);
	 * if (data == null)
	 * {
	 *     data = context.setSessionData(KEY, new SessionData());
	 * }
	 * </code>
	 * </pre>
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return the old value if already present, or the new value
	 */
	<T extends Serializable> T setSessionData(MetaDataKey<T> key, T value);

	/**
	 * Get data from the session.
	 * 
	 * @param key
	 *            key
	 * @return value
	 */
	<T extends Serializable> T getSessionData(MetaDataKey<T> key);

	/**
	 * Bind the current session. This make a temporary session become persistent across requests.
	 */
	void bind();

	/**
	 * Get the identifier of the session.
	 * 
	 * @return session id
	 */
	String getSessionId();
}