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
package org.apache.wicket.request.handler;

/**
 * Kludge mixin until we can get this properly merged into {@link IPageProvider} in a later version
 * of Wicket
 * 
 * @author igor
 */
// TODO wicket.next merge into IPageProvider
public interface IIntrospectablePageProvider
{
	/**
	 * Checks whether or not the provider has a page instance. This page instance might have been
	 * passed to this page provider directly or it may have been instantiated or retrieved from the
	 * page store.
	 * 
	 * @return {@code true} iff page instance has been created or retrieved
	 */
	public boolean hasPageInstance();

	/**
	 * Returns whether or not the page instance held by this provider has been instantiated by the
	 * provider.
	 * 
	 * @throws IllegalStateException
	 *             if this method is called and the provider does not yet have a page instance, ie
	 *             if {@link #getPageInstance()} has never been called on this provider
	 * @return {@code true} iff the page instance held by this provider was instantiated by the
	 *         provider
	 */
	public boolean isPageInstanceFresh();

}
