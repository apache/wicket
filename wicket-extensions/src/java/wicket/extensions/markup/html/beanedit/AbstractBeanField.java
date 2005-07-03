/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicaable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.beanedit;

import java.io.Serializable;

/**
 * Baseclass for fields.
 *
 * @author Eelco Hillenius
 */
public abstract class AbstractBeanField implements Serializable
{
	/** Logical name of the field. By default used for displaying */
	private final String name;

	/** name to be displayed. */
	private String displayName;

	/**
	 * Construct; uses the name as the name to display.
	 * @param name logical name of the field
	 */
	public AbstractBeanField(String name)
	{
		this(name, null);
	}

	/**
	 * Construct; uses the name as the name to display.
	 * @param name logical name of the field
	 * @param displayName name to display
	 */
	public AbstractBeanField(String name, String displayName)
	{
		this.name = name;
		this.displayName = displayName;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the name for displaying. Override for returning a custom (localized?) name.
	 * @return the name for displaying
	 */
	public String getDisplayName()
	{
		return (displayName != null) ? displayName : name;
	}

	/**
	 * Sets the name for displaying.
	 * @param displayName name for displaying
	 * @return This
	 */
	public AbstractBeanField setDisplayName(String displayName)
	{
		this.displayName = displayName;
		return this;
	}
}
