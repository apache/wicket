/*
 * $Id: RenderedDynamicImageResource.java,v 1.4 2005/03/08 21:12:40
 * jonathanlocke Exp $ $Revision$ $Date$
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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import wicket.util.time.Time;

/**
 * A DynamicImageResource subclass that allows easy rendering of regenenerable
 * (unbuffered) dynamic images. A RenderedDynamicImageResource implements the
 * abstract method render(Graphics2D) to create/re-create a given image
 * on-the-fly. When a RenderedDynamicImageResource is serialized, the image
 * state is transient, which means it will disappear when the resource is sent
 * over the wire and then will be recreated when required.
 * <p>
 * The extension/format of the image resource can be specified with
 * setFormat(String).
 *
 * @see wicket.markup.html.image.resource.DefaultButtonImageResource
 * @see wicket.markup.html.image.resource.DefaultButtonImageResourceFactory
 * @author Jonathan Locke
 * @author Gili Tzabari
 */
public abstract class RenderedDynamicImageResource extends DynamicImageResource
{
	/** Height of image */
	private int height = 100;

	/** Transient image data so that image only needs to be generated once per VM */
	private transient SoftReference imageData;

	/** Type of image (one of BufferedImage.TYPE_*) */
	private int type = BufferedImage.TYPE_INT_RGB;

	/** Width of image */
	private int width = 100;

	/**
	 * Constructor.
	 *
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 */
	public RenderedDynamicImageResource(final int width, final int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * @return Returns the type (one of BufferedImage.TYPE_*).
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Causes the image to be redrawn the next time its requested.
	 */
	public void invalidate()
	{
		super.invalidate();
		synchronized (this)
		{
			imageData = null;
		}
	}

	/**
	 * @param height
	 *            The height to set.
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}

	/**
	 * @param type
	 *            The type to set (one of BufferedImage.TYPE_*).
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	/**
	 * @param width
	 *            The width to set.
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}

	/**
	 * @return The image data for this dynamic image
	 */
	protected byte[] getImageData()
	{
		// Prevent image data from getting flushed while we access it
		byte[] data;
		synchronized (this)
		{
			if (imageData!=null)
				data = (byte[]) imageData.get();
			else
				data = null;
		}
		if (data == null)
		{
			data = render();
			synchronized (this)
			{
				imageData = new SoftReference(data);
			}
			setLastModifiedTime(Time.now());
		}
		return data;
	}

	/**
	 * Renders this image
	 *
	 * @return The image data
	 */
	protected byte[] render()
	{
		while (true)
		{
			final BufferedImage image = new BufferedImage(width, height, type);
			if (render((Graphics2D)image.getGraphics()))
			{
				return toImageData(image);
			}
		}
	}

	/**
	 * Override this method to provide your rendering code
	 *
	 * @param graphics
	 *            The graphics context to render on
	 * @return True if the image was rendered. False if the image size was
	 *         changed by the rendering implementation and the image should be
	 *         re-rendered at the new size.
	 */
	protected abstract boolean render(Graphics2D graphics);
}
