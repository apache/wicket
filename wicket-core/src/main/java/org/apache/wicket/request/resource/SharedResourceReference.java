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
package org.apache.wicket.request.resource;

import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;

/**
 * A {@link ResourceReference} which should be used to lookup a {@link IResource} from the globally
 * registered ones (also known as application shared resources). If there is no shared resource with
 * such {@link Key key} then it checks whether there is a {@link PackageResource} with this
 * {@link Key key} and registers it automatically if it exists.
 * <p>
 * Note: Cannot be registered in {@link ResourceReferenceRegistry} because
 * {@link SharedResourceReference} is just a shortcut to the {@link IResource resource} of another
 * {@link ResourceReference}
 */
public class SharedResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component specific variation of the style
	 */
	public SharedResourceReference(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		super(scope, name, locale, style, variation);
	}

	/**
	 * Construct.
	 * 
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 */
	public SharedResourceReference(Class<?> scope, String name)
	{
		super(scope, name);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            resource name
	 */
	public SharedResourceReference(String name)
	{
		super(name);
	}

	@Override
	public IResource getResource()
	{
		ResourceReference ref = Application.get()
			.getResourceReferenceRegistry()
			.getResourceReference(getScope(), getName(), getLocale(), getStyle(), getVariation(),
				false, true);

		if (ref == null)
		{
			return new AbstractResource()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ResourceResponse newResourceResponse(Attributes attributes)
				{
					ResourceResponse res = new ResourceResponse();
					res.setError(HttpServletResponse.SC_NOT_FOUND);
					return res;
				}
			};
		}
		else if (ref != this)
		{
			return ref.getResource();
		}
		else
		{
			throw new IllegalStateException(
				"SharedResourceReference can not be registered globally. See the documentation of this class.");
		}
	}

	@Override
	public boolean canBeRegistered()
	{
		return false;
	}
}
