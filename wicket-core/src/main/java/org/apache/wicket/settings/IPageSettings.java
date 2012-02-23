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
package org.apache.wicket.settings;

import java.util.List;

import org.apache.wicket.markup.resolver.IComponentResolver;


/**
 * Interface for page related settings.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public interface IPageSettings
{
	/**
	 * Adds a component resolver to the list.
	 * 
	 * @param resolver
	 *            The {@link IComponentResolver} that is added
	 */
	void addComponentResolver(IComponentResolver resolver);

	/**
	 * Get the (modifiable) list of IComponentResolvers.
	 * 
	 * @return List of ComponentResolvers
	 */
	List<IComponentResolver> getComponentResolvers();

	/**
	 * @return whether all pages should should update their page id when their component hierarchy
	 *      changes somehow
	 */
	boolean getVersionPagesByDefault();

	/**
	 * A global setting that tells the pages to update their page id if their component
	 * hierarchy changes somehow. This way versioned pages can have several versions
	 * stored in the page stores and the user can go back and forth through the different
	 * versions. If a page is not versioned then only its last state is keep in the page
	 * stores and going back will lead the user to the page before the current one, not
	 * to the previous state of the current one.
	 *
	 * @param pagesVersionedByDefault
	 *      a flag that indicates whether pages should increase their page id when
	 *      their component hierarchy changes somehow.
	 */
	void setVersionPagesByDefault(boolean pagesVersionedByDefault);

	/**
	 * When enabled (default), urls on mounted pages will contain the full mount path, including
	 * PageParameters, allowing wicket to reinstantiate the page if got expired. When disabled, urls
	 * only use the page id. If this setting is enabled, you should take care that names form fields
	 * on mounted pages do not clash with the page parameters.
	 * 
	 * @return if urls on mounted pages should be the full mount path
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4014">WICKET-4014</a>
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-4290">WICKET-4290</a>
	 */
	boolean getRecreateMountedPagesAfterExpiry();

	/**
	 * Sets the recreateMountedPagesAfterExpiry setting
	 * 
	 * @param recreateMountedPagesAfterExpiry
	 */
	void setRecreateMountedPagesAfterExpiry(boolean recreateMountedPagesAfterExpiry);
}