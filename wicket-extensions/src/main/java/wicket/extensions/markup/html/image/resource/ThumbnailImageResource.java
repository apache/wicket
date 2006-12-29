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
package wicket.extensions.markup.html.image.resource;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.WicketRuntimeException;
import wicket.markup.html.WebResource;
import wicket.markup.html.image.resource.DynamicImageResource;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.time.Time;

/**
 * Image resource that dynamically scales the given original resource to a
 * thumbnail. It is scaled either using the given maxSize as width or height,
 * depending on its shape. If both the width and height are less than maxSize,
 * no scaling is performed.
 * 
 * @author Eelco Hillenius
 */
public class ThumbnailImageResource extends DynamicImageResource
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(ThumbnailImageResource.class);

	/** the unscaled, original image resource. */
	private final WebResource unscaledImageResource;


	/** maximum size (width or height) for resize operation. */
	private final int maxSize;

	/** hint(s) for the scale operation. */
	private int scaleHints = Image.SCALE_SMOOTH;

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
	public ThumbnailImageResource(WebResource unscaledImageResource, int maxSize)
	{
		super();
		if (unscaledImageResource == null)
		{
			throw new IllegalArgumentException("Argument unscaledImageResource must be not null");
		}
		this.unscaledImageResource = unscaledImageResource;
		this.maxSize = maxSize;
	}

	/**
	 * @return The image data for this dynamic image
	 */
	@Override
	protected byte[] getImageData()
	{
		if (thumbnail == null)
		{
			final BufferedImage image = getScaledImageInstance();
			thumbnail = toImageData(image);
			setLastModifiedTime(Time.now());
		}
		return thumbnail;
	}

	/**
	 * get resized image instance.
	 * 
	 * @return BufferedImage
	 */
	protected final BufferedImage getScaledImageInstance()
	{
		InputStream is = null;
		BufferedImage originalImage = null;
		try
		{
			// read original image
			is = unscaledImageResource.getResourceStream().getInputStream();
			originalImage = ImageIO.read(is);
			if (originalImage == null)
			{
				throw new IOException("unable to read image");
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new WicketRuntimeException(e);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					log.error(e.getMessage(), e);
				}
			}
		}

		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		if (originalWidth > maxSize || originalHeight > maxSize)
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
			Image image = originalImage.getScaledInstance(newWidth, newHeight, scaleHints);

			// convert Image to BufferedImage
			BufferedImage bufferedImage = new BufferedImage(newWidth, newHeight,
					BufferedImage.TYPE_INT_BGR);
			bufferedImage.createGraphics().drawImage(image, 0, 0, null);

			return bufferedImage;
		}

		// no need for resizing
		return originalImage;
	}

	/**
	 * Sets hint(s) for the scale operation.
	 * 
	 * @param scaleHints
	 *            hint(s) for the scale operation
	 */
	public synchronized final void setScaleHints(int scaleHints)
	{
		this.scaleHints = scaleHints;
		invalidate();
	}

	/**
	 * @see wicket.markup.html.DynamicWebResource#invalidate()
	 */
	@Override
	public synchronized void invalidate()
	{
		thumbnail = null;
	}
}