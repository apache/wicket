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
package wicket.markup.html.image.resource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.util.lang.Packages;
import wicket.util.resource.IResourceStream;

/**
 * An image component represents a localizable image resource. The image name
 * comes from the src attribute of the image tag that the component is attached
 * to. The image component responds to requests made via IResourceListener's
 * resourceRequested method. The image or subclass responds by returning an
 * IResource from getImageResource(String), where String is the source attribute
 * of the image tag.
 * 
 * @author Jonathan Locke
 */
public class StaticImageResource extends ImageResource
{
	/** Map from key to resource */
	private static Map imageResourceMap = new HashMap();

	/** The image resource */
	private transient IResourceStream resource;

	/** The path to the resource */
	final String absolutePath;

	/** The resource's locale */
	final Locale locale;

	/** The resource's style */
	final String style;

	/**
	 * Gets the image resource for a given set of criteria. Only one image
	 * resource will be loaded for the same criteria.
	 * 
	 * @param basePackage
	 *            The base package to search from
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the image
	 * @param style
	 *            The style of the image
	 * @return The image resource
	 */
	public static StaticImageResource get(final Package basePackage, final String path,
			final Locale locale, final String style)
	{
		final String localeKeyPart = (locale != null) ? locale.toString() : "";
		final String localeStylePart = (style != null) ? style : "";
		final String key = basePackage.getName() + path + localeKeyPart + localeStylePart;
		synchronized (imageResourceMap)
		{
			StaticImageResource imageResource = (StaticImageResource)imageResourceMap.get(key);
			if (imageResource == null)
			{
				imageResource = new StaticImageResource(basePackage, path, locale, style);
				imageResourceMap.put(key, imageResource);
			}
			return imageResource;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param basePackage
	 *            The base package to search from
	 * @param path
	 *            The path to the resource
	 * @param locale
	 *            The locale of the image
	 * @param style
	 *            The style of the image
	 */
	private StaticImageResource(final Package basePackage, final String path, final Locale locale,
			final String style)
	{
		// Convert resource path to absolute path relative to base package
		this.absolutePath = Packages.absolutePath(basePackage, path);
		this.locale = locale;
		this.style = style;
	}

	/**
	 * @return Gets the image resource for the component.
	 */
	public IResourceStream getResourceStream()
	{
		if (resource == null)
		{
			// Locate resource
			this.resource = RequestCycle.get().getApplication().getResourceStreamLocator().locate(
					absolutePath, style, locale, null);
			
			// Check that resource was found
			if (this.resource == null)
			{
				throw new WicketRuntimeException("Unable to find static image resource [path = " + absolutePath + ", style = " + style + ", locale = " + locale + "]");
			}
		}
		return resource;
	}
}
