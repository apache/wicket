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

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.image.resource.ImageResource;
import wicket.markup.html.image.resource.LocalizedImageResource;
import wicket.model.IModel;
import wicket.model.Model;

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
	private final LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * @see wicket.Component#Component(String)
	 */
	public Image(final String id)
	{
		super(id);
	}

	/**
	 * Constructs an image directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public Image(final String id, final ImageResource imageResource)
	{
		super(id);
		setImageResource(imageResource);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * @param id
	 *            See Component
	 * @param string
	 *            Name of image
	 * @see wicket.Component#Component(String, IModel)
	 */
	public Image(final String id, final String string)
	{
		this(id, new Model(string));
	}

	/**
	 * @see AbstractImage#getResourcePath()
	 */
	public String getResourcePath()
	{
		return localizedImageResource.getImageResource().getPath();
	}

	/**
	 * @param imageResource
	 *            The new ImageResource to set.
	 */
	public void setImageResource(final ImageResource imageResource)
	{
		this.localizedImageResource.setImageResource(imageResource);
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
