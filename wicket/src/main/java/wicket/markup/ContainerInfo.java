/*
 * $Id$ $Revision:
 * 1.19 $ $Date$
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
package wicket.markup;

import java.util.Locale;

import wicket.MarkupContainer;

/**
 * Because a Component has reference to its parents, which eventually is the
 * Page, and because the Page contains a reference to the Session, keeping a
 * "copy" of a component is very expensive. ContainerInfo shall be used instead
 * of MarkupContainer whenever a small subset of the container's information is
 * required.
 * 
 * @author Juergen Donnerstag
 */
public class ContainerInfo
{
	private final Class containerClass;
	private final Locale locale;
	private final String style;
	private final String variation;
	private final String fileExtension;

	/**
	 * Construct.
	 * 
	 * @param container
	 *            The container to create the information from
	 */
	public ContainerInfo(final MarkupContainer container)
	{
		this.containerClass = container.getClass();
		this.locale = container.getLocale();
		this.style = container.getStyle();
		this.variation = null;
		this.fileExtension = container.getMarkupType();
	}

	/**
	 * 
	 * @return The container class
	 */
	public Class getContainerClass()
	{
		return containerClass;
	}

	/**
	 * 
	 * @return The container markup type (== file extension)
	 */
	public String getFileExtension()
	{
		return fileExtension;
	}

	/**
	 * 
	 * @return The container locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * 
	 * @return The container style
	 */
	public String getStyle()
	{
		return style;
	}

	/**
	 * 
	 * @return The containers variation
	 */
	public String getVariation()
	{
		return variation;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return containerClass.getName() + ":" + locale + ":" + style + ":" + fileExtension;
	}
}
