/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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
package wicket.markup.html.form;

import wicket.markup.ComponentTag;
import wicket.markup.html.image.resource.DefaultButtonImageResource;
import wicket.markup.html.image.resource.ImageResource;
import wicket.markup.html.image.resource.LocalizedImageResource;

/**
 * A button which renders itself as an image button resource.
 * 
 * @author Jonathan Locke
 */
public class ImageButton extends Button
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/** The image resource this image component references */
	private LocalizedImageResource localizedImageResource = new LocalizedImageResource(this);

	/**
	 * @see wicket.Component#Component(String)
	 */
	public ImageButton(String id)
	{
		super(id);
	}

	/**
	 * Constructs an image button directly from an image resource.
	 * 
	 * @param id
	 *            See Component
	 * 
	 * @param imageResource
	 *            The image resource
	 */
	public ImageButton(final String id, final ImageResource imageResource)
	{
		super(id);
		this.localizedImageResource.setImageResource(imageResource);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param label
	 *            The button label
	 */
	public ImageButton(final String id, final String label)
	{
		this(id, new DefaultButtonImageResource(label));
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected final void onComponentTag(final ComponentTag tag)
	{
		checkComponentTagAttribute(tag, "type", "image");

		// Try to load image resource from src attribute if not already loaded
		localizedImageResource.loadImageResource(tag);
		super.onComponentTag(tag);
	}
}
