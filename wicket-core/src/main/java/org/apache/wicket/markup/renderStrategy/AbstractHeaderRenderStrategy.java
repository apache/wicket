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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.util.lang.Args;

/**
 * An abstract implementation of a header render strategy which is only missing the code to traverse
 * the child hierarchy, since the sequence of that traversal is what will make the difference
 * between the different header render strategies.
 * 
 * Besides the child hierarchy the render sequence by default (may be changed via subclassing) is as
 * follows:
 * <ul>
 * <li>1. application level headers</li>
 * <li>2. the root component's headers</li>
 * <li>3. the children hierarchy (to be implemented per subclass)</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractHeaderRenderStrategy implements IHeaderRenderStrategy
{
	/**
	 * @return Gets the strategy registered with the application
	 */
	public static IHeaderRenderStrategy get()
	{
		// NOT OFFICIALLY SUPPORTED BY WICKET
		// By purpose it is "difficult" to change to another render strategy.
		// We don't want it to be modifiable by users, but we needed a way to easily test other
		// strategies.
		String className = System.getProperty("Wicket_HeaderRenderStrategy");
		if (className != null)
		{
			Class<?> clazz = null;
			try
			{
				clazz = Application.get()
					.getApplicationSettings()
					.getClassResolver()
					.resolveClass(className);

				if (clazz != null)
				{
					return (IHeaderRenderStrategy)clazz.newInstance();
				}
			}
			catch (ClassNotFoundException ex)
			{
				// ignore
			}
			catch (InstantiationException ex)
			{
				// ignore
			}
			catch (IllegalAccessException ex)
			{
				// ignore
			}
		}

		// Our default header render strategy
		// Pre 1.5
		// return new ParentFirstHeaderRenderStrategy();

		// Since 1.5
		return new ChildFirstHeaderRenderStrategy();
	}

	/**
	 * Construct.
	 */
	public AbstractHeaderRenderStrategy()
	{
	}

	public void renderHeader(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		Args.notNull(headerContainer, "headerContainer");
		Args.notNull(rootComponent, "rootComponent");

		// First the application level headers
		renderApplicationLevelHeaders(headerContainer);

		// Than the root component's headers
		renderRootComponent(headerContainer, rootComponent);

		// Than its child hierarchy
		renderChildHeaders(headerContainer, rootComponent);
	}

	/**
	 * Render the root component (e.g. Page).
	 * 
	 * @param headerContainer
	 * @param rootComponent
	 */
	protected void renderRootComponent(final HtmlHeaderContainer headerContainer,
		final Component rootComponent)
	{
		rootComponent.renderHead(headerContainer);
	}

	/**
	 * Render the child hierarchy headers.
	 * 
	 * @param headerContainer
	 * @param rootComponent
	 */
	abstract protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,
		final Component rootComponent);

	/**
	 * Render the application level headers
	 * 
	 * @param headerContainer
	 */
	protected final void renderApplicationLevelHeaders(final HtmlHeaderContainer headerContainer)
	{
		Args.notNull(headerContainer, "headerContainer");

		if (Application.exists())
		{
			for (IHeaderContributor listener : Application.get()
				.getHeaderContributorListenerCollection())
			{
				listener.renderHead(headerContainer.getHeaderResponse());
			}
		}
	}
}
