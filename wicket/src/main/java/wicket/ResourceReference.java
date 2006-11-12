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
package wicket;

import java.io.Serializable;
import java.util.Locale;

import wicket.markup.html.PackageResource;
import wicket.util.lang.Objects;

/**
 * ResourceReference is essentially a reference to an actual resource which is
 * shared through the Application. A ResourceReference has a name and a scope
 * (within which the name must be unique). It may also have a locale or style.
 * The locale and/or style do not need to be set on a resource reference because
 * those values will automatically be determined based on the context in which
 * the resource is being used. For example, if a ResourceReference is attached
 * to an Image component, when the locale for the page switches, the Image
 * component will notice this and automatically change the locale for the
 * referenced resource as appropriate. It's for this reason that you don't
 * typically have to use the constructor overloads taking a Locale or style
 * (these details are essentially internal and so the framework uses
 * setLocale/setStyle internally so you don't have to worry about it).
 * <p>
 * Package resources (resources which can be pulled from the classpath) do not
 * have to be pre-registered. For custom situations though, resources may be
 * added to the Application when the Application is constructed using
 * {@link Application#getSharedResources()} followed by
 * {@link SharedResources#add(Class, String, Locale, String, Resource)},
 * {@link SharedResources#add(String, Locale, Resource)}or
 * {@link SharedResources#add(String, Resource)}.
 * <p>
 * If a component has its own shared resource which should not be added to the
 * application construction logic in this way, it can lazy-initialize the
 * resource by overriding the {@link #newResource()} method. In this method, the
 * component should supply logic that creates the shared resource. By default
 * the {@link #newResource()} method tries to resolve to a package resource.
 * 
 * @author Jonathan Locke
 */
public class ResourceReference implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** The locale of the resource */
	protected Locale locale;

	/** The name of the resource */
	private final String name;

	/** The actual resource */
	private transient Resource resource;

	/** The scope of the named resource */
	private final Class scope;

	/** The style of the resource */
	private String style;

	/**
	 * Constructs a ResourceReference with the given scope and name. The scope
	 * is used as a namespace and the scope together with the name must uniquely
	 * identify the reference.
	 * 
	 * @param scope
	 *            The scope of the name
	 * @param name
	 *            The name of the resource
	 */
	public ResourceReference(final Class scope, final String name)
	{
		this(scope, name, null, null);
	}

	/**
	 * Constructs a ResourceReference with the given scope and name. The scope
	 * is used as a namespace and the scope together with the name must uniquely
	 * identify the reference. This constructor takes in the locale and style
	 * arguments. The locale might be overruled if this resource resolves to a
	 * package resource.
	 * 
	 * @param scope
	 *            The scope of the name
	 * @param name
	 *            The name of the resource
	 * @param locale
	 *            The Locale from which the search for the PackageResource must
	 *            start
	 * @param style
	 *            The Style of the PackageResource
	 */
	public ResourceReference(final Class scope, final String name, Locale locale, String style)
	{
		this.scope = scope;
		this.name = name;
		this.locale = locale;
		this.style = style;
	}

	/**
	 * Contructs a resource reference with Application.class scope and the given
	 * name. All resource references constructed with this constructor must have
	 * unique names since they all have the same Application-wide scope that is
	 * the wicket.Application.class
	 * 
	 * @param name
	 *            The name of the resource
	 */
	public ResourceReference(final String name)
	{
		this(Application.class, name);
	}

	/**
	 * Binds this shared resource to the given application.
	 * 
	 * @param application
	 *            The application which holds the shared resource
	 */
	public final void bind(final Application application)
	{
		// Try to resolve resource
		if (resource == null)
		{
			SharedResources sharedResources = application.getSharedResources();
			// Try to get resource from Application repository
			resource = sharedResources.get(scope, name, locale, style, true);

			// Not available yet?
			if (resource == null)
			{
				// Create resource using lazy-init factory method
				resource = newResource();
				if (resource == null)
				{
					// If lazy-init did not create resource with correct locale
					// and style then we should default the resource
					resource = sharedResources.get(scope, name, locale, style, false);
					if (resource == null)
					{
						// still null? try to see whether it is a package
						// resource that should
						// be lazily loaded
						PackageResource packageResource = PackageResource.get(scope, name);
						// will throw an exception if not found, so if we come
						// here, it was found
						sharedResources.add(name, packageResource);
					}
				}

				// Share through application
				sharedResources.add(scope, name, locale, style, resource);
			}
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof ResourceReference)
		{
			ResourceReference that = (ResourceReference)obj;
			return Objects.equal(this.scope, that.scope) && Objects.equal(this.name, that.name)
					&& Objects.equal(this.locale, that.locale)
					&& Objects.equal(this.style, that.style);
		}
		return false;
	}

	/**
	 * @return Returns the locale.
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * @return Name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Gets the resource for this resource reference. If the ResourceReference
	 * has not yet been bound to the application via
	 * {@link ResourceReference#bind(Application)}this method may return null.
	 * 
	 * @return The resource, or null if the ResourceReference has not yet been
	 *         bound.
	 */
	public final Resource getResource()
	{
		return resource;
	}

	/**
	 * @return Scope
	 */
	public final Class getScope()
	{
		return scope;
	}

	/**
	 * @return the shared resource key for this resource reference.
	 */
	public final String getSharedResourceKey()
	{
		Application application = Application.get();
		bind(application);
		return application.getSharedResources().resourceKey(scope, name, locale, style);
	}

	/**
	 * @return Returns the style. (see {@link wicket.Session})
	 */
	public final String getStyle()
	{
		return style;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = 17;
		result = 37 * result + (scope != null ? scope.hashCode() : 0);
		result = 37 * result + (name != null ? name.hashCode() : 0);
		result = 37 * result + (locale != null ? locale.hashCode() : 0);
		result = 37 * result + (style != null ? style.hashCode() : 0);
		return result;
	}

	/**
	 * Sets any loaded resource to null, thus forcing a reload on the next
	 * request.
	 */
	public final void invalidate()
	{
		this.resource = null;
	}

	/**
	 * @param locale
	 *            The locale to set.
	 */
	public final void setLocale(Locale locale)
	{
		this.locale = locale;
		invalidate();
	}

	/**
	 * @param style
	 *            The style to set (see {@link wicket.Session}).
	 */
	public final void setStyle(String style)
	{
		this.style = style;
		invalidate();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[ResourceReference name = " + name + ", scope = " + scope + ", locale = " + locale
				+ ", style = " + style + "]";
	}

	/**
	 * Factory method for lazy initialization of shared resources.
	 * 
	 * @return The resource
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
			throw new IllegalArgumentException("package resource [scope=" + getScope() + ",name="
					+ getName() + ",locale=" + getLocale() + "style=" + getStyle() + "] not found");
		}
		return packageResource;
	}
}
