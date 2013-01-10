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
package org.apache.wicket.atmosphere;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Listener interface for intercepting the registration of Atmosphere resources for pages. For every
 * page that has an Atmosphere resource, {@link #resourceRegistered(String, Page)} will be called.
 * When the suspended connection is terminated (by a page unload, session termination or a closed
 * connection), {@link #resourceUnregistered(String)} is invoked.
 * 
 * @author papegaaij
 */
public interface ResourceRegistrationListener
{
	/**
	 * Invoked when a new suspended connection is setup and registered for a page. This method is
	 * invoked in the context of a wicket request where the {@link RequestCycle} and {@link Session}
	 * are available. The {@code Page} is attached and it is safe to call methods on it. However,
	 * you should never keep a reference to the page. Not only will this create memory leaks, but
	 * can only be accessed from the context of a Wicket request on that page.
	 * 
	 * @param uuid
	 * @param page
	 */
	public void resourceRegistered(String uuid, Page page);

	/**
	 * Invoked when a suspended connection is terminated and unregistered.
	 * 
	 * @param uuid
	 */
	public void resourceUnregistered(String uuid);
}
