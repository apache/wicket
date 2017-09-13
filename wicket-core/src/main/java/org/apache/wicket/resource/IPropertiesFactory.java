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
package org.apache.wicket.resource;

import org.apache.wicket.util.value.ValueMap;


/**
 * Implementations are responsible for {@link #load(Class, String) locating} {@link Properties}
 * objects, which are a thin wrapper around {@link ValueMap} and is used to locate localized
 * messages.
 * <p>
 * The {@link #clearCache()} method should remove any cached references to properties objects used
 * by an implementation, so that {@link #load(Class, String)} gets the freshest instance possible.
 * </p>
 * <p>
 * {@link IPropertiesChangeListener Listeners} are related to cached properties and should be used
 * to inform observers when sets of properties are reloaded.
 * </p>
 * 
 * @see org.apache.wicket.resource.Properties
 * 
 * @author Juergen Donnerstag
 */
public interface IPropertiesFactory
{
	/**
	 * Add a listener which will be called when a change to the underlying resource stream (e.g.
	 * properties file) has been detected
	 * 
	 * @param listener
	 */
	void addListener(final IPropertiesChangeListener listener);

	/**
	 * Remove all cached properties.
	 */
	void clearCache();

	/**
	 * Load the properties associated with the path
	 * 
	 * @param clazz
	 *            The class requesting the properties
	 * @param path
	 *            The path to identify the resource
	 * @return The properties
	 */
	Properties load(final Class<?> clazz, final String path);
}
