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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;

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
 * @author Juergen Donnerstag
 */
public abstract class ResourceReference implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final Key data;

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param key
	 *            The data making up the resource reference
	 */
	public ResourceReference(final Key key)
	{
		Args.notNull(key, "key");

		data = key;
	}

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 *            resource locale
	 * @param style
	 *            resource style
	 * @param variation
	 *            resource variation
	 */
	public ResourceReference(Class<?> scope, String name, Locale locale, String style,
		String variation)
	{
		Args.notNull(scope, "scope");
		Args.notNull(name, "name");

		data = new Key(scope.getName(), name, locale, style, variation);
	}

	/**
	 * Creates new {@link ResourceReference} instance.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 */
	public ResourceReference(Class<?> scope, String name)
	{
		this(scope, name, null, null, null);
	}

	/**
	 * Construct.
	 * 
	 * @param name
	 *            resource name
	 */
	public ResourceReference(String name)
	{
		this(Application.class, name, null, null, null);
	}

	/**
	 * @return Gets the data making up the resource reference. They'll be use by
	 *         ResourceReferenceRegistry to make up the key under which the resource reference gets
	 *         stored.
	 */
	public final Key getKey()
	{
		return data;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return data.getName();
	}

	/**
	 * returns extension of the resource reference
	 * 
	 * @return extension of the resource's name in lower-case or <code>null</code> if there is no
	 *         extension
	 */
	public final String getExtension()
	{
		String name = getName();

		final int queryAt = name.indexOf('?');

		// remove query string part
		if (queryAt != -1)
		{
			name = name.substring(0, queryAt);
		}

		// get start of extension
		final int extPos = name.lastIndexOf('.');

		if (extPos == -1)
		{
			return null;
		}

		// return extension
		return name.substring(extPos + 1).toLowerCase();
	}

	/**
	 * @return scope
	 */
	public Class<?> getScope()
	{
		return WicketObjects.resolveClass(data.getScope());
	}

	/**
	 * @return locale
	 */
	public Locale getLocale()
	{
		return data.getLocale();
	}

	/**
	 * @return style
	 */
	public String getStyle()
	{
		return data.getStyle();
	}

	/**
	 * @return variation
	 */
	public String getVariation()
	{
		return data.getVariation();
	}

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
		return Objects.equal(data, that.data);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return data.hashCode();
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
		 *            resource locale
		 * @param style
		 *            resource style
		 * @param variation
		 *            resource variation
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
			if (obj instanceof UrlAttributes == false)
			{
				return false;
			}
			UrlAttributes that = (UrlAttributes)obj;
			return Objects.equal(getLocale(), that.getLocale()) &&
				Objects.equal(getStyle(), that.getStyle()) &&
				Objects.equal(getVariation(), that.getVariation());
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return Objects.hashCode(getLocale(), getStyle(), getVariation());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "locale: " + locale + "; style: " + style + "; variation: " + variation;
		}
	}

	/**
	 * A (re-usable) data store for all relevant ResourceReference data
	 */
	public static class Key implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final String scope;
		private final String name;
		private final Locale locale;
		private final String style;
		private final String variation;

		/**
		 * Construct.
		 * 
		 * @param reference
		 *            resource reference
		 */
		public Key(final ResourceReference reference)
		{
			this(reference.getScope().getName(), reference.getName(), reference.getLocale(),
				reference.getStyle(), reference.getVariation());
		}

		/**
		 * Construct.
		 * 
		 * @param scope
		 *            resource scope
		 * @param name
		 *            resource name
		 * @param locale
		 *            resource locale
		 * @param style
		 *            resource style
		 * @param variation
		 *            resource variation
		 */
		public Key(final String scope, final String name, final Locale locale, final String style,
			final String variation)
		{
			Args.notNull(scope, "scope");
			Args.notNull(name, "name");

			this.scope = scope.intern();
			this.name = name.intern();
			this.locale = locale;
			this.style = style != null ? style.intern() : null;
			this.variation = variation != null ? variation.intern() : null;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof Key == false)
			{
				return false;
			}
			Key that = (Key)obj;
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
		 * Gets scope.
		 * 
		 * @return scope
		 */
		public final String getScope()
		{
			return scope;
		}

		/**
		 * @return Assuming scope ist a fully qualified class name, than get the associated class
		 */
		public final Class<?> getScopeClass()
		{
			return WicketObjects.resolveClass(scope);
		}

		/**
		 * Gets name.
		 * 
		 * @return name
		 */
		public final String getName()
		{
			return name;
		}

		/**
		 * Gets locale.
		 * 
		 * @return locale
		 */
		public final Locale getLocale()
		{
			return locale;
		}

		/**
		 * Gets style.
		 * 
		 * @return style
		 */
		public final String getStyle()
		{
			return style;
		}

		/**
		 * Gets variation.
		 * 
		 * @return variation
		 */
		public final String getVariation()
		{
			return variation;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return "scope: " + scope + "; name: " + name + "; locale: " + locale + "; style: " +
				style + "; variation: " + variation;
		}
	}

	@Override
	public String toString()
	{
		return data.toString();
	}

	/**
	 * @return the resources this ResourceReference depends on.
	 */
	public List<HeaderItem> getDependencies()
	{
		return new ArrayList<>();
	}
}
