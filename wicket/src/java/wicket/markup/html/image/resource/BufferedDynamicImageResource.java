/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
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

import java.awt.image.BufferedImage;

import wicket.util.time.Time;


/**
 * An image subclass that allows easy rendering of dynamic images. An image can
 * be set with setImage(BufferedImage) and its extension can be specified with
 * setExtension(String). After this, the image will be cached as an input stream
 * and will render as would any other Image resource.
 * 
 * @author Jonathan Locke
 */
public class BufferedDynamicImageResource extends DynamicImageResource
{
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/** The byte array holding the contents of the dynamic image */
	private byte[] imageData;

	/** The time this image resource was last modified */
	private Time lastModifiedTime;

	/**
	 * @param image
	 *            The image to set
	 */
	public void setImage(final BufferedImage image)
	{
		imageData = toImageData(image);
		lastModifiedTime = Time.now();
	}

	/**
	 * @see wicket.markup.html.image.resource.DynamicImageResource#getImageData()
	 */
	protected byte[] getImageData()
	{
		return imageData;
	}
	
	/**
	 * @see wicket.markup.html.image.resource.DynamicImageResource#lastModifiedTime()
	 */
	public Time lastModifiedTime()
	{
		return lastModifiedTime;
	}
}
