/*
 * $Id$ $Revision:
 * 1.27 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceLocator;

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

	/** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	/** The image resource */
	private IResource resource;

	/**
	 * Gets the image resource for a given set of criteria. Only one image
	 * resource will be loaded for the same criteria.
	 * 
	 * @param classLoader
	 *            The classloader for loading the image
	 * @param basePackage
	 *            The base package to search from
	 * @param resourcePath
	 *            The path to the resource
	 * @param locale
	 *            The locale of the image
	 * @param style
	 *            The style of the image
	 * @return The image resource
	 */
	public static StaticImageResource get(final ClassLoader classLoader, final Package basePackage,
			final String resourcePath, final Locale locale, final String style)
	{
		final String key = classLoader.toString() + basePackage.getName() + resourcePath
				+ locale.toString() + style;
		synchronized (imageResourceMap)
		{
			StaticImageResource imageResource = (StaticImageResource)imageResourceMap.get(key);
			if (imageResource == null)
			{
				imageResource = new StaticImageResource(classLoader, basePackage, resourcePath, locale,
						style);
				imageResourceMap.put(key, imageResource);
			}
			return imageResource;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param classLoader
	 *            The classloader for loading the image
	 * @param basePackage
	 *            The base package to search from
	 * @param resourcePath
	 *            The path to the resource
	 * @param locale
	 *            The locale of the image
	 * @param style
	 *            The style of the image
	 */
	private StaticImageResource(final ClassLoader classLoader, final Package basePackage,
			final String resourcePath, final Locale locale, final String style)
	{
		// TODO we might want to consider relaxing this in the future so people
		// can stash images in subfolders and the like
		if (resourcePath.indexOf("..") != -1 || resourcePath.indexOf("/") != -1)
		{
			throw new WicketRuntimeException("Source for image resource cannot contain a path");
		}

		final String path = basePackage.getName() + "." + resourcePath;
		this.resource = ResourceLocator.locate(RequestCycle.get().getApplication().getSettings()
				.getSourcePath(), classLoader, path, style, locale, null);
	}

	/**
	 * @return Gets the image resource for the component.
	 */
	protected IResource getResource()
	{
		return resource;
	}
}
