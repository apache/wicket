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
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * This has been Wicket's default header render strategy before WICKET 1.5 which uses
 * {@link MarkupContainer#visitChildren(org.apache.wicket.util.visit.IVisitor)} to traverse the hierarchy to
 * render the children headers.
 * 
 * Since child contributions are added to the markup after the parent contributions, children may
 * replace / modify existing settings. Which is not good. Instead the parent (container) should be
 * in control (see <a href="https://issues.apache.org/jira/browse/WICKET-2693">WICKET-2693</a>).
 * 
 * @author Juergen Donnerstag
 */
public class ParentFirstHeaderRenderStrategy extends AbstractHeaderRenderStrategy
{
	/**
	 * Construct.
	 */
	public ParentFirstHeaderRenderStrategy()
	{
	}

	@Override
	protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		Args.notNull(headerContainer, "headerContainer");
		Args.notNull(rootComponent, "rootComponent");

		// Only MarkupContainer can have children. Component's don't
		if (rootComponent instanceof MarkupContainer)
		{
			// Visit the children with parent first, than children
			((MarkupContainer)rootComponent).visitChildren(new IVisitor<Component, Void>()
			{
				@Override
				public void component(final Component component, final IVisit<Void> visit)
				{
					if (component.isVisibleInHierarchy())
					{
						component.internalRenderHead(headerContainer);
					}
					else
					{
						visit.dontGoDeeper();
					}
				}
			});
		}
	}
}
