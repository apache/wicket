/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.image.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;


/**
 * A DynamicImageResource subclass that allows easy rendering of regeneratable (unbuffered) dynamic
 * images. A RenderedDynamicImageResource implements the abstract method render(Graphics2D) to
 * create/re-create a given image on-the-fly. When a RenderedDynamicImageResource is serialized, the
 * image state is transient, which means it will disappear when the resource is sent over the wire
 * and then will be recreated when required.
 * <p>
 * The format of the image (and therefore the resource's extension) can be specified with
 * setFormat(String). The default format is "PNG" because JPEG is lossy and makes generated images
 * look bad and GIF has patent issues.
 * 
 * @see org.apache.wicket.markup.html.image.resource.DefaultButtonImageResource
 * @see org.apache.wicket.markup.html.image.resource.DefaultButtonImageResourceFactory
 * @author Jonathan Locke
 * @author Gili Tzabari
 * @author Johan Compagner
 */
public abstract class RenderedDynamicImageResource extends DynamicImageResource
{
	private static final long serialVersionUID = 1L;

	/** Height of image */
	private int height = 100;

	/** Transient image data so that image only needs to be generated once per VM */
	private transient SoftReference<byte[]> imageData;

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
	 * Constructor.
	 * 
	 * @param width
	 *            Width of image
	 * @param height
	 *            Height of image
	 * @param format
	 *            The format of the image (jpg, png or gif)
	 */
	public RenderedDynamicImageResource(final int width, final int height, String format)
	{
		super(format);
		this.width = width;
		this.height = height;
	}

	/**
	 * @return Returns the height.
	 */
	public synchronized int getHeight()
	{
		return height;
	}

	/**
	 * @return Returns the type (one of BufferedImage.TYPE_*).
	 */
	public synchronized int getType()
	{
		return type;
	}

	/**
	 * @return Returns the width.
	 */
	public synchronized int getWidth()
	{
		return width;
	}

	/**
	 * Causes the image to be redrawn the next time its requested.
	 */
	public synchronized void invalidate()
	{
		imageData = null;
	}

	/**
	 * @param height
	 *            The height to set.
	 */
	public synchronized void setHeight(int height)
	{
		this.height = height;
		invalidate();
	}

	/**
	 * @param type
	 *            The type to set (one of BufferedImage.TYPE_*).
	 */
	public synchronized void setType(int type)
	{
		this.type = type;
		invalidate();
	}

	/**
	 * @param width
	 *            The width to set.
	 */
	public synchronized void setWidth(int width)
	{
		this.width = width;
		invalidate();
	}

	@Override
	protected byte[] getImageData(Attributes attributes)
	{
		// get image data is always called in sync block
		byte[] data = null;
		if (imageData != null)
		{
			data = imageData.get();
		}
		if (data == null)
		{
			data = render(attributes);
			imageData = new SoftReference<byte[]>(data);
			setLastModifiedTime(Time.now());
		}
		return data;
	}

	/**
	 * Renders this image
	 * 
	 * @param attributes
	 *            the current request attributes
	 * @return The image data
	 */
	protected byte[] render(final Attributes attributes)
	{
		while (true)
		{
			final BufferedImage image = new BufferedImage(getWidth(), getHeight(), getType());
			if (render((Graphics2D)image.getGraphics(), attributes))
			{
				return toImageData(image);
			}
		}
	}

	/**
	 * Override this method to provide your rendering code.
	 * 
	 * @param graphics
	 *            The graphics context to render on.
	 * @param attributes
	 *            the current request attributes
	 * @return {@code true} if the image was rendered. {@code false} if the image size was changed
	 *         by the rendering implementation and the image should be re-rendered at the new size.
	 */
	protected abstract boolean render(Graphics2D graphics, final Attributes attributes);
}
