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

/**
 * Allows for different header render strategies. The difference per strategy will be order in which
 * components will be asked to add to the markup header section. It might be page->container->child,
 * it might be child->container->parent, but it might also be a mixture depending on your specific
 * application needs.
 * 
 * @author Juergen Donnerstag
 */
public interface IHeaderRenderStrategy
{
	/**
	 * Implements the render strategy
	 * 
	 * @param headerContainer
	 * @param component
	 */
	void renderHeader(final HtmlHeaderContainer headerContainer, final Component component);
}
