/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * An image subclass that allows easy rendering of dynamic images. An image can
 * be set with setImage(BufferedImage) and its extension can be specified with
 * setExtension(String). After this, the image will be cached as an input stream
 * and will render as would any other Image resource.
 * 
 * @author Jonathan Locke
 */
public abstract class DynamicImage extends AbstractDynamicImage
{
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/** Height of image */
	private final int height;

	/** Width of image */
	private final int width;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Component name
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 */
	public DynamicImage(String name, int width, int height)
	{
		super(name);
		this.width = width;
		this.height = height;
	}

	/**
	 * @return The image data for this dynamic image
	 */
	public byte[] getImageData()
	{		
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		render((Graphics2D)image.getGraphics());
		return toImageData(image);
	}

	/**
	 * Override this method to provide your rendering code
	 * @param graphics The graphics context to render on
	 */
	protected abstract void render(Graphics2D graphics);
}
