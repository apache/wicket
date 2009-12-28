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
	 * @param markupStream
	 * @param tag
	 * @return <code>null</code> if a component was could not be found
	 */
	public static Component resolve(final MarkupContainer container,
		final MarkupStream markupStream, final ComponentTag tag)
	{
		// try to resolve using component hierarchy

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

		// fallback to application-level resolvers

		for (final IComponentResolver resolver : Application.get()
			.getPageSettings()
			.getComponentResolvers())
		{
			Component component = resolver.resolve(container, markupStream, tag);
			if (component != null)
			{
				return component;
			}
		}

		return null;
	}
}
