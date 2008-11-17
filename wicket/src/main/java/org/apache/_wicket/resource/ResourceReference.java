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
package org.apache._wicket.resource;

import java.util.Locale;

import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.Objects;

/**
 * Reference to a resource. Can be used to reference global resources.
 * 
 * @author Matej Knopp
 */
public abstract class ResourceReference
{
	private final String scope;
	private final String name;
	private final Locale locale;
	private String style;

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 * @param style
	 */
	public ResourceReference(Class<?> scope, String name, Locale locale, String style)
	{
		if (scope == null)
		{
			throw new IllegalArgumentException("Argument 'scope' can not be null.");
		}
		this.scope = scope.getName();

		if (name == null)
		{
			throw new IllegalArgumentException("Argument 'path' can not be null.");
		}
		this.name = name;
		this.locale = locale;
		this.style = style;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return scope
	 */
	public Class<?> getScope()
	{
		return Classes.resolveClass(scope);
	}

	/**
	 * @return locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * @return style
	 */
	public String getStyle()
	{
		return style;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof ResourceReference == false)
		{
			return false;
		}
		ResourceReference that = (ResourceReference)obj;
		return Objects.equal(scope, that.scope) && //
			Objects.equal(name, that.name) && //
			Objects.equal(locale, that.locale) && //
			Objects.equal(style, that.style);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(scope, name, locale, style);
	}
	
	/**
	 * Creates new resource.
	 * 
	 * @return new resource instance
	 */
	public abstract Resource getResource();
}
