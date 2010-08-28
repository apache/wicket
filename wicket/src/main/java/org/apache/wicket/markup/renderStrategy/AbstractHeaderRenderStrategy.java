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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Beside the child hierarchy the render sequence by default (may be changed via subclassing) is as
 * follows:
 * <ul>
 * <li>1. application level headers</li>
 * <li>2. the root component's headers</li>
 * <li>3. the childs hierarchy (to be implemented per subclass)</li>
 * </ul>
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractHeaderRenderStrategy implements IHeaderRenderStrategy
{
	/** Application level contributors */
	private List<IHeaderContributor> renderHeadListeners;

	/** It is not in IRenderSettings since it is highly experimental only */
	private static IHeaderRenderStrategy strategy;

	/**
	 * @return Gets the strategy registered with the application
	 */
	public static IHeaderRenderStrategy get()
	{
		if (strategy == null)
		{
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
						strategy = (IHeaderRenderStrategy)clazz.newInstance();
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
		}

		if (strategy == null)
		{
			strategy = new ParentFirstHeaderRenderStrategy();
		}

		return strategy;
	}

	/**
	 * Construct.
	 */
	public AbstractHeaderRenderStrategy()
	{
	}

	/**
	 * @see org.apache.wicket.markup.renderStrategy.IHeaderRenderStrategy#renderHeader(org.apache.wicket.markup.html.internal.HtmlHeaderContainer,
	 *      org.apache.wicket.Component)
	 */
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

		if (renderHeadListeners != null)
		{
			for (Iterator<IHeaderContributor> iter = renderHeadListeners.iterator(); iter.hasNext();)
			{
				IHeaderContributor listener = iter.next();
				listener.renderHead(headerContainer.getHeaderResponse());
			}
		}
	}

	/**
	 * Add an application level contributor who's content will be added to any page or ajax
	 * response.
	 * 
	 * @param contributor
	 */
	public final void addListener(final IHeaderContributor contributor)
	{
		if (renderHeadListeners == null)
		{
			renderHeadListeners = new ArrayList<IHeaderContributor>();
		}
		renderHeadListeners.add(contributor);
	}

	/**
	 * Remove an application level contributor
	 * 
	 * @param contributor
	 */
	public void removeListener(final IHeaderContributor contributor)
	{
		if (renderHeadListeners != null)
		{
			renderHeadListeners.remove(contributor);
			if (renderHeadListeners.isEmpty())
			{
				renderHeadListeners = null;
			}
		}
	}
}
