/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
 * A named resource, with optional scoping, that wraps any Resource for shared
 * access. It has a transient Resource member which can be refreshed when needed
 * from the Application by calling Application.getResource(Class scope, String
 * name).
 * 
 * @author Jonathan Locke
 */
public final class SharedResource extends Resource
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
	 * Sets any loaded resource to null, thus forcing a reload on the next
	 * request.
	 */
	public void invalidate()
	{
		this.resource = null;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
		invalidate();
	}
	
	/**
	 * @param style The style to set.
	 */
	public void setStyle(String style)
	{
		this.style = style;
		invalidate();
	}

	/**
	 * @return Gets the resource to render to the requester
	 */
	protected final IResource getResource()
	{
		if (resource == null)
		{
			this.resource = Session.get().getApplication().getResource(scope, name, locale, style);
		}
		return resource.getResource();
	}
}
