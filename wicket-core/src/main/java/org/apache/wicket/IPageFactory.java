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
package org.apache.wicket;

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * A factory class that creates Pages. A Page can be created by Class, with or without a
 * PageParameters argument to pass to the Page's constructor.
 * <p>
 * IMPORTANT NOTE: Implementations must let subclasses of
 * {@link org.apache.wicket.request.flow.ResetResponseException} thrown from the constructing page's
 * constructor bubble up.
 * 
 * @see org.apache.wicket.Application#newPageFactory()
 * @see Session#getPageFactory()
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public interface IPageFactory
{
	/**
	 * Creates a new page using a page class.
	 * 
	 * @param <C>
	 *          the type of the page class
	 * @param pageClass
	 *            The page class to instantiate
	 * @return The page
	 * @throws WicketRuntimeException
	 *             Thrown if the page cannot be constructed
	 */
	<C extends IRequestablePage> C newPage(final Class<C> pageClass);

	/**
	 * Creates a new Page, passing PageParameters to the Page constructor if such a constructor
	 * exists. If no such constructor exists and the parameters argument is null or empty, then any
	 * available default constructor will be used.
	 * 
	 * @param <C>
	 *          the type of the page class
	 * @param pageClass
	 *            The class of Page to create
	 * @param parameters
	 *            Any parameters to pass to the Page's constructor
	 * @return The new page
	 * @throws WicketRuntimeException
	 *             Thrown if the page cannot be constructed
	 */
	<C extends IRequestablePage> C newPage(final Class<C> pageClass, final PageParameters parameters);

	/**
	 * Checks whether a page can be instantiated using a bookmarkable URL.
	 * 
	 * @param <C>
	 *            the type of the page class
	 * @param pageClass
	 *            The class of page to check for bookmarkability
	 * 
	 * @return {@code true} if the page can be instantiated by this {@link IPageFactory}
	 */
	<C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass);
}
