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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Component.IVisit;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.util.lang.Checks;

/**
 * THIS IS EXPERIMENTAL ONLY. YOU MUST NOT USE IT IN YOUR APPLICATION. SOME WICKET CORE COMPONENT
 * WILL NOT WORK PROPERLY. THIS CLASS MAY AS WELL BE REMOVED AGAIN.
 * 
 * This a header render strategy which sequence is child->parent->root, and thus inverse to Wicket's
 * default implementation. To your application it means, that parent containers can effectively
 * replace child contributions, since their contribution is added to the markup after the child
 * ones.
 * 
 * Please note that irrespective of the render strategy, if the same header content (e.g. CSS file)
 * gets added twice to the header, only the first will be rendered and the 2nd will skipped.
 * 
 * @author Juergen Donnerstag
 */
public class ChildFirstHeaderRenderStrategy extends AbstractHeaderRenderStrategy
{
	/**
	 * Construct.
	 */
	public ChildFirstHeaderRenderStrategy()
	{
	}

	/**
	 * @see org.apache.wicket.markup.renderStrategy.AbstractHeaderRenderStrategy#renderHeader(org.apache.wicket.markup.html.internal.HtmlHeaderContainer,
	 *      org.apache.wicket.Component)
	 */
	@Override
	public void renderHeader(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		Checks.argumentNotNull(headerContainer, "headerContainer");
		Checks.argumentNotNull(rootComponent, "rootComponent");

		// First the application level headers
		renderApplicationLevelHeaders(headerContainer);

		// Than its child hierarchy
		renderChildHeaders(headerContainer, rootComponent);

		// Than the root component's headers
		renderRootComponent(headerContainer, rootComponent);
	}

	/**
	 * Render the child hierarchy headers.
	 * 
	 * @param headerContainer
	 * @param rootComponent
	 */
	@Override
	protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		Checks.argumentNotNull(headerContainer, "headerContainer");
		Checks.argumentNotNull(rootComponent, "rootComponent");

		if (rootComponent instanceof MarkupContainer)
		{
			new DeepChildFirstVisitor()
			{
				@Override
				public void component(final Component component,
					final IVisit<Component> visit)
				{
					component.renderHead(headerContainer);
				}
			}.visit(rootComponent);
		}
	}

	/**
	 * In case you need mixed strategies depending on the component, you can subclass this method
	 * and return true when traversing shall stop of that specific component.
	 * 
	 * This check happens <b>before</b> the component's header gets rendered
	 * 
	 * @param component
	 * @return true, if traversal shall stop with that component
	 */
	protected boolean stopTraversingBefore(final Component component)
	{
		return false;
	}

	/**
	 * In case you need mixed strategies depending on the component, you can subclass this method
	 * and return true when traversing shall stop of that specific component.
	 * 
	 * This check happens <b>after</b> the component's header gets rendered
	 * 
	 * @param component
	 * @return true, if traversal shall stop with that component
	 */
	protected boolean stopTraversingAfter(final Component component)
	{
		return false;
	}
}
