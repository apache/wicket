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

import java.io.Serializable;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.util.lang.WicketObjects;

/**
 * Reference to a resource. Can be used to reference global resources.
 * <p>
 * Even though resource reference is just a factory for resources, it still needs to be identified
 * by a globally unique identifier, combination of <code>scope</code> and <code>name</code>. Those
 * are used to generate URLs for resource references. <code>locale</code>, <code>style</code> and
 * <code>variation</code> are optional fields to allow having specific references for individual
 * locales, styles and variations.
 * 
 * @author Matej Knopp
 */
public abstract class ResourceReference implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String scope;
	private final String name;
	private final Locale locale;
	private final String style;
	private final String variation;

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public ResourceReference(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		Args.notNull(scope, "scope");
		Args.notNull(name, "name");

		this.scope = scope.getName();
		this.name = name;
		this.locale = locale;
		this.style = style;
		this.variation = variation;
	}

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 * @param style
	 * @param variation
	 */
	public ResourceReference(Class<?> scope, String name)
	{
		this(scope, name, null, null, null);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ResourceReference(String name)
	{
		this(Application.class, name, null, null, null);
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
		return WicketObjects.resolveClass(scope);
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

	/**
	 * @return variation
	 */
	public String getVariation()
	{
		return variation;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
			Objects.equal(style, that.style) && //
			Objects.equal(variation, that.variation);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Objects.hashCode(scope, name, locale, style, variation);
	}

	/**
	 * Returns the resource.
	 * 
	 * @return resource instance
	 */
	public abstract IResource getResource();

	/**
	 * Allows to specify which locale, style and variation values will the generated URL for this
	 * resource reference have.
	 * 
	 * @return url attributes
	 */
	public UrlAttributes getUrlAttributes()
	{
		return new UrlAttributes(getLocale(), getStyle(), getVariation());
	}

	/**
	 * @see ResourceReference#getUrlAttributes()
	 * 
	 * @author Matej Knopp
	 */
	public static class UrlAttributes
	{
		private final Locale locale;
		private final String style;
		private final String variation;

		/**
		 * Construct.
		 * 
		 * @param locale
		 * @param style
		 * @param variation
		 */
		public UrlAttributes(Locale locale, String style, String variation)
		{
			this.locale = locale;
			this.style = style;
			this.variation = variation;
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

		/**
		 * @return variation
		 */
		public String getVariation()
		{
			return variation;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof UrlAttributes == false)
			{
				return false;
			}
			UrlAttributes that = (UrlAttributes)obj;
			return Objects.equal(getLocale(), that.getLocale()) &&
				Objects.equal(getStyle(), that.getStyle()) &&
				Objects.equal(getVariation(), that.getVariation());
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(getLocale(), getStyle(), getVariation());
		}
	};

	/**
	 * Can be used to disable registering certain resource references in
	 * {@link ResourceReferenceRegistry}.
	 * 
	 * @return <code>true</code> if this reference can be registered, <code>false</code> otherwise.
	 */
	public boolean canBeRegistered()
	{
		return true;
	}
}
