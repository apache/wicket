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
package org.apache.wicket.request.component;

import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Base interface for pages. The purpose of this interface is to make certain parts of Wicket easier
 * to mock and unit test.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IRequestablePage extends IRequestableComponent, IManageablePage
{
	/**
	 * Renders the page
	 */
	void renderPage();

	/**
	 * Bookmarkable page can be instantiated using a bookmarkable URL.
	 * 
	 * @return Returns true if the page is bookmarkable.
	 */
	boolean isBookmarkable();

	/**
	 * Returns the number of times this page has been rendered. The number will be appended to
	 * listener interface links in order to prevent invoking listeners from staled page version.
	 * <p>
	 * For example a same page might have been rendered in two separate tabs. Page render doesn't
	 * change page id but it can modify component hierarchy. Listener interface links on such page
	 * should only work in tab where the page was rendered most recently.
	 * 
	 * @return render count
	 */
	int getRenderCount();

	/**
	 * Returns whether the page instance was created by a bookmarkable URL. Non mounted pages have
	 * to be created using bookmarkable URL in order to have hybrid URLs later. Otherwise it would
	 * be a potential security risk.
	 * 
	 * @return <code>true</code> if this page has been created by a bookmarkable URL,
	 *         <code>false</code> otherwise.
	 */
	boolean wasCreatedBookmarkable();

	/**
	 * Returns the {@link PageParameters} for the page. Each bookmarkable page instance should have
	 * {@link PageParameters} associated with it. The page parameters are initialized from URL when
	 * page is created and are updated on every page render request.
	 * 
	 * @return page parameters or <code>null</code>
	 */
	PageParameters getPageParameters();
}
