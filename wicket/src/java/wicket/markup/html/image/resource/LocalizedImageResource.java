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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.Component;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.string.Strings;

/**
 * This class contains the logic for extracting static image resources
 * referenced by the src attribute of component tags and keeping these static
 * image resources in sync with the component locale.
 * 
 * @author Jonathan Locke
 */
public class LocalizedImageResource implements Serializable
{
	/** Map from image factory names to image factories */
	private static final Map nameToImageFactory = new HashMap();
	
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/** The component that is referencing this image resource */
	private Component component;

	/** The image resource this image component references */
	private ImageResource imageResource;

	/** The locale of the image resource */
	private Locale locale;

	/**
	 * Adds an image resource factory to the list of factories to consult when
	 * generating images
	 * 
	 * @param imageFactory
	 *            The image factory to add
	 */
	public static void add(final ImageResourceFactory imageFactory)
	{
		nameToImageFactory.put(imageFactory.getName(), imageFactory);
	}

	/**
	 * Constructor
	 * 
	 * @param component
	 *            The component that owns this localized image resource
	 */
	public LocalizedImageResource(final Component component)
	{
		this.component = component;
	}

	/**
	 * @return Returns the imageResource.
	 */
	public ImageResource getImageResource()
	{
		return imageResource;
	}

	/**
	 * @param tag
	 *            The tag to inspect for an optional src attribute that might
	 *            reference an image.
	 * @throws WicketRuntimeException
	 *             Thrown if an image is required by the caller, but none can be
	 *             found.
	 */
	public void loadImageResource(final ComponentTag tag)
	{
		// If locale has changed from the initial locale used to attach image
		// resource, then we need to reload the resource in the new locale
		if (locale != null && locale != component.getLocale())
		{
			imageResource = null;
		}

		// Need to load image resource for this component?
		if (imageResource == null)
		{
			// Get model string
			final String modelString = component.getModelObjectAsString();

			// If model string is empty, use src attribute of tag
			final String resourcePath;
			if (Strings.isEmpty(modelString))
			{
				resourcePath = tag.getString("src");
			}
			else
			{
				resourcePath = modelString;
			}

			// If we found a resource path, try to load the image resource
			if (!Strings.isEmpty(resourcePath))
			{
				final Package basePackage = component.findParentWithAssociatedMarkup().getClass()
						.getPackage();
				this.imageResource = StaticImageResource.get(basePackage, resourcePath, component
						.getLocale(), component.getStyle());
			}

			// If we can't get a static image and one isn't already assigned
			if (imageResource == null)
			{
				setImageResource(generateImageResource(tag));
			}

			this.locale = component.getLocale();
		}

		final String url = ((WebRequestCycle)component.getRequestCycle()).urlFor(imageResource
				.getPath());
		tag.put("src", component.getResponse().encodeURL(url).replaceAll("&", "&amp;"));
	}

	/**
	 * @param imageResource
	 *            The imageResource to set.
	 */
	public void setImageResource(ImageResource imageResource)
	{
		this.imageResource = imageResource;
	}

	/**
	 * Generates an image resource based on the attribute values on tag
	 * 
	 * @param tag
	 *            The tag to look at
	 * @return The image resource
	 */
	private ImageResource generateImageResource(final ComponentTag tag)
	{
		// Get label from value attribute
		String label = tag.getString("value");
		if (label == null)
		{
			throw new WicketRuntimeException(
					"Component was not assigned an ImageResource, and had neither a src "
							+ "attribute referencing a static image nor a value "
							+ "attribute from which to generate an image.");
		}

		// Generate a button image from the value attribute
		if (label.indexOf(':') != -1)
		{
			// Get factory name
			final String imageFactoryName = Strings.beforeFirst(label, ':');
			
			// Look up factory
			final ImageResourceFactory factory = (ImageResourceFactory)nameToImageFactory
					.get(imageFactoryName);
			
			// Found factory?
			if (factory == null)
			{
				throw new WicketRuntimeException("Could not find image resource factory named "
						+ imageFactoryName);
			}
			
			// Get value to pass to factory
			final String imageLabel = Strings.afterFirst(label, ':');
			
			// Have factory create new labelled image
			return factory.imageResource(imageLabel);
		}
		else
		{
			throw new WicketRuntimeException(
					"Could not find or generate image from label \"" + label + "\".  Was expecting either a static image reference or a value or title attribute of the form \"imageResourceFactoryName.label\".");
		}
	}

	static
	{
		add(new DefaultButtonImageResourceFactory("buttonFactory"));
	}
}
