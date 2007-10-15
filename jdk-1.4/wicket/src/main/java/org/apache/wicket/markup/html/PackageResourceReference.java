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
package org.apache.wicket.markup.html;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;


/**
 * A convenience class for creating resource references to static resources.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * 
 * @deprecated Use {@link ResourceReference} instead. This class will be removed in Wicket 2.0
 */
public class PackageResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	/** pre-calculated has code for this immutable object. */
	private int hash;

	/**
	 * Constructor to get a resource reference to a packaged resource.
	 * 
	 * @param application
	 *            The application to bind to
	 * @param scope
	 *            The scope of the binding
	 * @param name
	 *            The name of the resource
	 * @param locale
	 *            The Locale from which the search for the PackageResource must start
	 * @param style
	 *            The Style of the PackageResource
	 * @throws IllegalArgumentException
	 *             when no corresponding resource is found
	 * 
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Application application, Class scope, String name,
			Locale locale, String style)
	{
		super(scope, name);
		checkExists(scope, name, locale, style);
		setHash(scope, name, locale, style);
		setLocale(locale);
		setStyle(style);
		bind(application);
		if (getResource() instanceof PackageResource)
		{
			setLocale(((PackageResource)getResource()).getLocale());
		}
	}

	/**
	 * Constructor to get a resource reference to a packaged resource.
	 * 
	 * @param application
	 *            The application to bind to
	 * @param scope
	 *            The scope of the binding
	 * @param name
	 *            The name of the resource
	 * @throws IllegalArgumentException
	 *             when no corresponding resource is found
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Application application, Class scope, String name)
	{
		this(application, scope, name, null, null);
	}

	/**
	 * Constructor to get a resource reference to a packaged resource.
	 * 
	 * The scope of this constructor will be the org.apache.wicket.Application.class itself. so the
	 * shared resources key will be "org.apache.wicket.Application/name"
	 * 
	 * @param application
	 *            The application to bind to
	 * @param name
	 *            The name of the resource
	 * @throws IllegalArgumentException
	 *             when no corresponding resource is found
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Application application, String name)
	{
		this(application, Application.class, name);
	}

	/**
	 * Constructor to get a resource reference to a packaged resource that is already bound to the
	 * current application.
	 * 
	 * @param scope
	 *            The scope of the binding
	 * @param name
	 *            The name of the resource
	 * @throws IllegalArgumentException
	 *             when no corresponding resource is found
	 * @see ResourceReference#ResourceReference(Class, String)
	 */
	public PackageResourceReference(Class scope, String name)
	{
		super(scope, name);
		// This can't be done in the constructor because ResourceReferences can
		// be declared static in a class that is stored in the session.
		// checkExists(scope, name, null, null);
		setHash(scope, name, null, null);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof PackageResourceReference)
		{
			PackageResourceReference that = (PackageResourceReference)obj;
			return checkEquals(getScope(), that.getScope()) &&
					checkEquals(getName(), that.getName()) &&
					checkEquals(getLocale(), that.getLocale()) &&
					checkEquals(getStyle(), that.getStyle());
		}
		return false;
	}

	/**
	 * @see org.apache.wicket.ResourceReference#newResource()
	 */
	protected Resource newResource()
	{
		PackageResource packageResource = PackageResource.get(getScope(), getName(), getLocale(),
				getStyle());
		if (packageResource != null)
		{
			locale = packageResource.getLocale();
		}
		else
		{
			throw new IllegalArgumentException("package resource [scope=" + getScope() + ",name=" +
					getName() + ",locale=" + getLocale() + "style=" + getStyle() + "] not found");
		}
		return packageResource;
	}

	/**
	 * Checks whether the packaged resource can be found. If it can't be found, an
	 * {@link IllegalArgumentException} will be thrown. If it was found, this method just returns.
	 * 
	 * @param scope
	 *            the scope of the resource
	 * @param name
	 *            the name of the resource
	 * @param locale
	 *            the optional locale for the resource
	 * @param style
	 *            the optional style for the resource
	 */
	private void checkExists(Class scope, String name, Locale locale, String style)
	{
		if (!PackageResource.exists(scope, name, locale, style))
		{
			throw new IllegalArgumentException("package resource [scope=" + scope + ",name=" +
					name + ",locale=" + locale + "style=" + style + "] not found");
		}
	}

	private final boolean checkEquals(Object o1, Object o2)
	{
		if (o1 == null)
		{
			return o2 == null;
		}
		else if (o2 == null)
		{
			return false;
		}
		else
		{
			return o1.equals(o2);
		}
	}

	private final void setHash(Class scope, String name, Locale locale, String style)
	{
		int result = 17;
		result = 37 * result + (scope != null ? scope.hashCode() : 0);
		result = 37 * result + (name != null ? name.hashCode() : 0);
		result = 37 * result + (locale != null ? locale.hashCode() : 0);
		result = 37 * result + (style != null ? style.hashCode() : 0);
		hash = result;
	}
}