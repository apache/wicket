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
package org.apache.wicket.markup.resolver;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;

/**
 * Utility class for {@link IComponentResolver}s
 * 
 * @author igor.vaynberg
 */
public class ComponentResolvers
{
	private ComponentResolvers()
	{
	}

	/**
	 * Attempts to resolve a component using resolvers. Tries resolvers in the component hierarchy
	 * as well as application-wide.
	 * <p>
	 * This method encapsulates the contract of resolving components and should be used any time a
	 * component needs to be resolved under normal circumstances.
	 * </p>
	 * 
	 * @param container
	 *            The container parsing its markup
	 * @param markupStream
	 *            The current markupStream
	 * @param tag
	 *            The current component tag while parsing the markup
	 * @param filter
	 *            A filter for application-wide resolvers
	 * @return component or {@code null} if not found
	 */
	public static Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag, final ResolverFilter filter)
	{
		// try to resolve using component hierarchy
		Component component = resolveByComponentHierarchy(container, markupStream, tag);

		if (component == null)
		{
			// fallback to application-level resolvers
			component = resolveByApplication(container, markupStream, tag, filter);
		}

		return component;
	}

	/**
	 * Attempts to resolve a component via application registered resolvers.
	 * 
	 * @param container
	 * @param markupStream
	 * @param tag
	 * @param filter
	 * @return Null, if no component was found
	 */
	private static Component resolveByApplication(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag, final ResolverFilter filter)
	{
		for (final IComponentResolver resolver : Application.get()
			.getPageSettings()
			.getComponentResolvers())
		{
			if ((filter == null) || (filter.ignoreResolver(resolver) == false))
			{
				Component component = resolver.resolve(container, markupStream, tag);
				if (component != null)
				{
					return component;
				}
			}
		}

		return null;
	}

	/**
	 * Attempts to resolve a component via the component hierarchy using resolvers.
	 * 
	 * @param container
	 * @param markupStream
	 * @param tag
	 * @return Null, if no component was found
	 */
	private static Component resolveByComponentHierarchy(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		Component cursor = container;
		while (cursor != null)
		{
			if (cursor instanceof IComponentResolver)
			{
				IComponentResolver resolver = (IComponentResolver)cursor;
				Component component = resolver.resolve(container, markupStream, tag);
				if (component != null)
				{
					return component;
				}
			}
			cursor = cursor.getParent();
		}

		return null;
	}

	/**
	 * 
	 */
	public interface ResolverFilter
	{
		/**
		 * 
		 * @param resolver
		 * @return true, if resolvers should be skipped
		 */
		boolean ignoreResolver(IComponentResolver resolver);
	}
}
