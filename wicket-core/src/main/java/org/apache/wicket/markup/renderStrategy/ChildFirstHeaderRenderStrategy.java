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
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer.HeaderStreamState;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;

/**
 * This a header render strategy implements a child->parent->root sequence, which is inverse to how
 * it was until Wicket 1.5. It now allows parent containers to replace child contributions, since
 * their contribution is added to the markup after the child ones (see <a
 * href="https://issues.apache.org/jira/browse/WICKET-2693">WICKET-2693</a>).
 * 
 * Please note that irrespective of the render strategy, if the same header content (e.g. CSS file)
 * gets added twice to the header, only the first will be rendered and the 2nd will be skipped.
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

	@Override
	public void renderHeader(final HtmlHeaderContainer headerContainer,
		HeaderStreamState headerStreamState, final Component rootComponent)
	{
		Args.notNull(headerContainer, "headerContainer");
		Args.notNull(rootComponent, "rootComponent");

		// First the application level headers
		renderApplicationLevelHeaders(headerContainer);

		// Then its child hierarchy
		renderChildHeaders(headerContainer, rootComponent);

		// Then the root component's headers
		renderRootComponent(headerContainer, headerStreamState, rootComponent);
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
		Args.notNull(headerContainer, "headerContainer");
		Args.notNull(rootComponent, "rootComponent");

		if (rootComponent instanceof MarkupContainer)
		{
			new DeepChildFirstVisitor()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					if (component != rootComponent)
					{
						component.internalRenderHead(headerContainer);
					}
				}

				@Override
				public boolean preCheck(Component component)
				{
					return component.isVisibleInHierarchy();
				}
			}.visit(rootComponent);
		}
	}
}
