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

import wicket.util.resource.IResource;

/**
 * SharedResource is essentially a reference to an actual resource which is
 * shared through Application. A SharedResource has a name and a scope (within
 * which the name must be unique). It may also have a locale or style.
 * <p>
 * SharedResources may be added to the Application when the Application is
 * constructed using
 * {@link Application#addResource(Class, String, Locale, String, Resource)},
 * {@link Application#addResource(String, Locale, Resource)}or
 * {@link Application#addResource(String, Resource)}.
 * <p>
 * If a component has its own shared resource which should not be added to the
 * application construction logic in this way, it can lazy-initialize the
 * resource by overriding the {@link SharedResource#newResource()}method. In
 * this method, the component should supply logic that creates the shared
 * resource.
 * 
 * @author Jonathan Locke
 */
public class SharedResource extends Resource
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
	public SharedResource(final Class scope, final String name)
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
	public SharedResource(final String name)
	{
		this(Application.class, name);
	}

	/**
	 * Adds this shared resource to the given application
	 * 
	 * @param application
	 *            The application
	 */
	public final void add(final Application application)
	{
		application.addResource(scope, name, locale, style, this);
	}

	/**
	 * @return Returns the locale.
	 */
	public final Locale getLocale()
	{
		return locale;
	}

	/**
	 * @return Path to this shared resource
	 */
	public final String getPath()
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append(scope.getName());
		buffer.append('_');
		buffer.append(name);
		if (locale != null)
		{
			buffer.append('_');
			buffer.append(locale.toString());
		}
		if (style != null)
		{
			buffer.append('_');
			buffer.append(style);
		}
		return buffer.toString();
	}

	/**
	 * @return Returns the style.
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
	 *            The style to set.
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
		return "[SharedResource name = " + name + ", scope = " + scope + ", locale = " + locale
				+ ", style = " + style + "]";
	}

	/**
	 * Binds this shared resource to the given application
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
					throw new WicketRuntimeException("Unable to resolve shared resource " + this);
				}

				// Share through application
				application.addResource(scope, name, locale, style, resource);
			}
		}
	}

	/**
	 * @see wicket.SharedResource#getResource()
	 */
	protected final IResource getResource()
	{
		return resource.getResource();
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
