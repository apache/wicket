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
package org.apache.wicket.ng.request.handler;

import org.apache.wicket.ng.request.component.PageExpiredException;
import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.mapper.StalePageException;


/**
 * Represents object capable of providing a page instance. In some cases the implementation class
 * might now page class and page parameters without having the actual page instance. Thus it's
 * recommended to call {@link #getPageParameters()} instead of calling {@link #getPageInstance()}
 * .getPageParameters(). Same goes for page class.
 * 
 * @author Matej Knopp
 */
public interface PageProvider
{
	/**
	 * Returns page instance specified by the constructor.
	 * 
	 * @return page instance
	 * @throws StalePageException
	 *             if render count has been specified in constructor and the render count of page
	 *             does not match the valeu
	 * @throw {@link PageExpiredException} if the specified page could not have been found and the
	 *        constructor used did not provide enough information to create new page instance
	 */
	public abstract RequestablePage getPageInstance();

	/**
	 * Returns {@link PageParametersNg} of the page.
	 * 
	 * @return page parameters
	 */
	public abstract PageParametersNg getPageParameters();

	/**
	 * Returns class of the page.
	 * 
	 * @return page class
	 */
	public abstract Class<? extends RequestablePage> getPageClass();

	/**
	 * Detaches the page if it has been loaded.
	 */
	public abstract void detach();

}