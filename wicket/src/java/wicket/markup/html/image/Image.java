/*
 * $Id$ $Revision:
 * 1.27 $ $Date$
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
package wicket.markup.html.image;

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.image.resource.ImageResource;
import wicket.markup.html.image.resource.LocalizedImageResource;

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
public class Image extends AbstractImage
{
	/** Serial Version ID */
	private static final long serialVersionUID = 555385780092173403L;

	/** The image resource this image component references */
	private LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * @see wicket.Component#Component(String)
	 */
	public Image(final String name)
	{
		super(name);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * @param name
	 *            See Component#Component(String)
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public Image(final String name, final ImageResource imageResource)
	{
		super(name);
		this.localizedImageResource.setImageResource(imageResource);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public Image(final String name, final Serializable object)
	{
		super(name, object);
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public Image(final String name, final Serializable object, final String expression)
	{
		super(name, object, expression);
	}

	/**
	 * @see AbstractImage#getResourcePath()
	 */
	public String getResourcePath()
	{
		return localizedImageResource.getImageResource().getPath();
	}

	/**
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		localizedImageResource.loadImageResource(tag);
		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	}
}
