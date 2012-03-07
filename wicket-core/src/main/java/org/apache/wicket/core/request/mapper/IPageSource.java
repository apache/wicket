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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Interface for objects that are capable of getting and creating page instance.
 *
 * @author Matej Knopp
 */
public interface IPageSource
{
	/**
	 * Returns existing page instance if the page exists.
	 *
	 * @param pageId
	 * @return page instance or <code>null</code> if the page does not exist.
	 */
	IRequestablePage getPageInstance(int pageId);

	/**
	 * Creates new page instance of page with given class. The page should be marked as create
	 * bookmarkable, so subsequent calls to {@link IRequestablePage#wasCreatedBookmarkable()} must
	 * return <code>true</code>
	 *
	 * @param pageClass
	 * @param pageParameters
	 * @return new page instance
	 */
	IRequestablePage newPageInstance(Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters);

}
