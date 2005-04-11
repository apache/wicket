/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.util.Locale;

/**
 * ResourceReference is essentially a reference to an actual resource which is
 * shared through the Application. A ResourceReference has a name and a scope
 * (within which the name must be unique). It may also have a locale or style.
 * The locale and/or style do not need to be set on a resource reference because
 * those values will automatically be determined based on the context in which
 * the resource is being used. For example, if a ResourceReference is attached
 * to an Image component, when the locale for the page switches, the Image
 * component will notice this and automatically change the locale for the
 * referenced resource as appropriate. It's for this reason that there are no
 * constructor overloads taking a Locale or style (these details are essentially
 * internal and so the framework uses setLocale/setStyle internally so you don't
 * have to worry about it).
 * <p>
 * Resources may be added to the Application when the Application is constructed
 * using
 * {@link Application#addResource(Class, String, Locale, String, Resource)},
 * {@link Application#addResource(String, Locale, Resource)}or
 * {@link Application#addResource(String, Resource)}.
 * <p>
 * If a component has its own shared resource which should not be added to the
 * application construction logic in this way, it can lazy-initialize the
 * resource by overriding the {@link ResourceReference#newResource()}method. In
 * this method, the component should supply logic that creates the shared
 * resource.
 * 
 * @author Jonathan Locke
 */
public class ResourceReference
{
	/** The locale of the resource */
	private Locale locale;

	/** The name of the resource */
	private final String name;

	/** The actual resource */
	private transient Resource resource;

	/** The scope of the named resource */
	private final Class scope;

	/** The style of the resource */
	private String style;

	/**
	 * Constructor
	 * 
	 * @param scope
	 *            The scope of the name
	 * @param name
	 *            The name of the resource
	 */
	public ResourceReference(final Class scope, final String name)
	{
		this.scope = scope;
		this.name = name;
	}

	/**
	 * Constructor
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
			// Try to get resource from Application repository
			resource = application.getResource(scope, name, locale, style);

			// Not available yet?
			if (resource == null)
			{
				// Create resource using lazy-init factory method
				resource = newResource();
				if (resource == null)
				{
					// If lazy-init did not create resource with correct locale
					// and style then we should default the resource
					resource = application.getResource(scope, name, null, null);
					if (resource == null)
					{
						throw new WicketRuntimeException("Unable to resolve shared resource "
								+ this);
					}
				}

				// Share through application
				application.addResource(scope, name, locale, style, resource);
			}
		}
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
	 * @return Path for this resource reference.
	 */
	public final String getPath()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append("resources/");
		buffer.append(scope.getName());
		buffer.append('_');
		buffer.append(Application.localizedPath(name, locale, style));
		return buffer.toString();
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
	 * @return Returns the style. (see {@link wicket.Session})
	 */
	public final String getStyle()
	{
		return style;
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
		return null;
	}
}
