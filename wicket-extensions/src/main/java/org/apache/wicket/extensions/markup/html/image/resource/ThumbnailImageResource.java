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
package org.apache.wicket.extensions.markup.html.image.resource;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.response.ByteArrayResponse;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * Image resource that dynamically scales the given original resource to a thumbnail. It is scaled
 * either using the given maxSize as width or height, depending on its shape. If both the width and
 * height are less than maxSize, no scaling is performed.
 * 
 * @author Eelco Hillenius
 * @author Eugene Kamenev
 */

public class ThumbnailImageResource extends DynamicImageResource
{
	private static final long serialVersionUID = 1L;

	/** the unscaled, original image resource. */
	private final IResource unscaledImageResource;

	/** maximum size (width or height) for resize operation. */
	private final int maxSize;

	/** the cached byte array of the thumbnail. */
	private transient byte[] thumbnail;

	/**
	 * Construct.
	 * 
	 * @param unscaledImageResource
	 *            the unscaled, original image resource. Must be not null
	 * @param maxSize
	 *            maximum size (width or height) for resize operation
	 */
	public ThumbnailImageResource(final IResource unscaledImageResource, final int maxSize)
	{
		Args.notNull(unscaledImageResource, "unscaledImageResource");
		
		this.unscaledImageResource = unscaledImageResource;
		this.maxSize = maxSize;
	}

	/**
	 * @return The image data for this dynamic image
	 */
	@Override
	protected byte[] getImageData(final Attributes attributes)
	{
		if (thumbnail == null)
		{
			final BufferedImage image = getScaledImageInstance(attributes);
			thumbnail = toImageData(image);
			setLastModifiedTime(Time.now());
		}
		return thumbnail;
	}

	/**
	 * get resized image instance.
	 * 
	 * @param attributes
	 * 
	 * @return BufferedImage
	 */
	protected BufferedImage getScaledImageInstance(final Attributes attributes)
	{
		InputStream is = null;
		BufferedImage originalImage = null;
		try
		{
			// read original image
			ByteArrayResponse byteResponse = new ByteArrayResponse();
			Attributes dispatchAttributes = new Attributes(attributes.getRequest(), byteResponse, attributes.getParameters());
			unscaledImageResource.respond(dispatchAttributes);
			is = new ByteArrayInputStream(byteResponse.getBytes());
			originalImage = ImageIO.read(is);
			if (originalImage == null)
			{
				throw new IOException("Unable to read unscaled image");
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
		finally
		{
			IOUtils.closeQuietly(is);
		}

		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		if ((originalWidth > maxSize) || (originalHeight > maxSize))
		{
			final int newWidth;
			final int newHeight;

			if (originalWidth > originalHeight)
			{
				newWidth = maxSize;
				newHeight = (maxSize * originalHeight) / originalWidth;
			}
			else
			{
				newWidth = (maxSize * originalWidth) / originalHeight;
				newHeight = maxSize;
			}

			// http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
			BufferedImage dimg = new BufferedImage(newWidth, newHeight, originalImage.getType());
			Graphics2D g = dimg.createGraphics();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(originalImage, 0, 0, newWidth, newHeight, 0, 0, originalWidth,
					originalHeight, null);
			}
			finally
			{
				g.dispose();
			}

			return dimg;
		}

		// no need for resizing
		return originalImage;
	}

}
