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

import org.apache.wicket.Localizer;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.watch.IModificationWatcher;

/**
 * environment required for properties factory
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public interface IPropertiesFactoryContext
{
	/**
	 * Get the application's localizer.
	 *
	 * to modify the way Wicket resolves keys to localized messages you can 
	 * add custom resource loaders to the list returned by 
	 * {@link org.apache.wicket.settings.IResourceSettings#getStringResourceLoaders()}.
	 *
	 * @return The application wide localizer instance
	 */
	Localizer getLocalizer();

	/**
	 * @return Resource locator for this application
	 */
	IResourceStreamLocator getResourceStreamLocator();

	/**
	 * @param start
	 *            boolean if the resource watcher should be started if not already started.
	 *
	 * @return Resource watcher with polling frequency determined by setting, or null if no polling
	 *         frequency has been set.
	 */
	IModificationWatcher getResourceWatcher(boolean start);
}
