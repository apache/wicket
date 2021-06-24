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
package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer.HeaderStreamState;

/**
 * Allows for different header render strategies. The difference per strategy will be order in which
 * components are asked to add to the markup header section. Before 1.5 it was
 * page-&gt;container-&gt;child. Since 1.5 it has been changed to child-&gt;container-&gt;parent (see <a
 * href="http://issues.apache.org/jira/browse/WICKET-2693 ">WICKET-2693</a>)
 * 
 * @author Juergen Donnerstag
 */
public interface IHeaderRenderStrategy
{
	/**
	 * Implements the render strategy
	 * 
	 * @param headerContainer
	 *            The HeaderContainer associated to the response
	 * @param headerStreamState
	 *            the header section of the page, when null, this section will not be rendered
	 * @param component
	 *            The root component (e.g. Page) to start the render process
	 */
	void renderHeader(HtmlHeaderContainer headerContainer, HeaderStreamState headerStreamState,
		Component component);
}
