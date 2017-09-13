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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.core.request.mapper.StalePageException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Represents object capable of providing a page instance. In some cases the implementation class
 * might now page class and page parameters without having the actual page instance. Thus it's
 * recommended to call {@link #getPageParameters()} instead of calling {@link #getPageInstance()}
 * .getPageParameters(). Same goes for page class.
 *
 * @author Matej Knopp
 */
public interface IPageProvider
{
	/**
	 * Returns page instance specified by the constructor.
	 *
	 * @return page instance
	 * @throws StalePageException
	 *             if render count has been specified in constructor and the render count of page
	 *             does not match the value
	 * @throws PageExpiredException if the specified page
     *          could not have been found and the constructor used did not provide enough information
     *          to create new page instance
	 */
	IRequestablePage getPageInstance();

	/**
	 * Returns {@link PageParameters} of the page.
	 *
	 * @return page parameters
	 */
	PageParameters getPageParameters();

	/**
	 * @return negates {@link PageProvider#hasPageInstance()}
	 * @deprecated use {@link PageProvider#hasPageInstance()} negation instead
	 */
	boolean isNewPageInstance();

	/**
	 * Returns whether the provided page was expired prior to this access.
	 *
	 * @return <code>true></code> if the page was created after its original instance expired.
	 */
	boolean wasExpired();

	/**
	 * Returns class of the page.
	 *
	 * @return page class
	 */
	Class<? extends IRequestablePage> getPageClass();

	/**
	 * Returns the page id.
	 *
	 * @return page id
	 */
	Integer getPageId();

	/**
	 * Returns the number of times this page has been rendered.
	 *
	 * @return the number of times this page has been rendered.
	 */
	Integer getRenderCount();

	/**
	 * Detaches the page if it has been loaded.
	 */
	void detach();

	/**
	 * If this provider returns existing page, regardless if it was already created by PageProvider
	 * itself or is or can be found in the data store. The only guarantee is that by calling
	 * {@link PageProvider#getPageInstance()} this provider will return an existing instance and no
	 * page will be created.
	 * 
	 * @return if provides an existing page
	 */
	boolean hasPageInstance();

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
	boolean doesProvideNewPage();
}
