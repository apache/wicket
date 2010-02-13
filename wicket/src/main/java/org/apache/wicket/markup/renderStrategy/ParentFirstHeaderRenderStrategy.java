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
import org.apache.wicket.util.lang.Checks;

/**
 * This is Wicket's default header render strategy which uses
 * {@link MarkupContainer#visitChildren(org.apache.wicket.Component.IVisitor)} to traverse the
 * hierarchy to render the children headers.
 * 
 * Since child contributions are added to the markup after the parent contributions, children may
 * replace / modify existing settings.
 * 
 * Note that in order to mix different render strategies, a "stop traversal" mechanism has been
 * implemented. It allows you to use strategy A for Wicket core components and strategy B for your
 * own.
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
			((MarkupContainer)rootComponent).visitChildren(new Component.IVisitor<Component>()
			{
				public Object component(Component component)
				{
					if (component.isVisibleInHierarchy())
					{
						component.renderHead(headerContainer);
						return CONTINUE_TRAVERSAL;
					}
					else
					{
						return CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
					}
				}
			});
		}
	}
}
