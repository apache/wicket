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

import java.util.Iterator;

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
	 * @param application
	 * @param container
	 * @param markupStream
	 * @param tag
	 * @return <code>true</code> if a component was resolved using on of tried resolvers,
	 *         <code>false</code> otherwise.
	 */
	public static boolean resolve(final Application application, final MarkupContainer container,
		MarkupStream markupStream, ComponentTag tag)
	{

		// try to resolve using component hierarchy

		Component cursor = container;
		while (cursor != null)
		{
			if (cursor instanceof IComponentResolver)
			{
				if (((IComponentResolver)cursor).resolve(container, markupStream, tag))
				{
					return true;
				}
			}
			cursor = cursor.findParent(MarkupContainer.class);
		}

		// fallback to application-level resolvers

		Iterator<IComponentResolver> resolvers = application.getPageSettings()
			.getComponentResolvers()
			.iterator();
		while (resolvers.hasNext())
		{
			IComponentResolver resolver = resolvers.next();
			if (resolver.resolve(container, markupStream, tag))
			{
				return true;
			}
		}

		return false;
	}

}
