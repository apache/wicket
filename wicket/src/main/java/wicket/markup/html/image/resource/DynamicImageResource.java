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
package wicket.markup.html.image.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import wicket.WicketRuntimeException;
import wicket.markup.html.DynamicWebResource;
import wicket.util.time.Time;

/**
 * An ImageResource subclass for dynamic images (images created
 * programmatically). Subclasses override getImageData() to provide the image
 * data to send back to the user. A given subclass may decide how to produce
 * this data and whether/how to buffer it.
 * <p>
 * The RenderedDynamicImageResource subclass is designed for images that can be
 * regenerated when the component is deserialized (the image data is transient).
 * A good example of a RenderedDynamicImageResource is the
 * DefaultButtonImageResource class, which can regenerate a given button image
 * at any time. This makes it very lightweight when clustered. The
 * BufferedDynamicImageResource class, on the other hand, is designed for images
 * that cannot be regenerated on demand. It buffers its image data in a
 * non-transient way, which means that the entire image will be serialized and
 * copied when the resource is replicated in a cluster!
 * <p>
 * The helper method toImageData(BufferedImage) is provided so that subclasses
 * can easily turn a BufferedImage into a suitable return value when
 * implementing getImageData().
 * <p>
 * The format of the image (and therefore the resource's extension) can be
 * specified with setFormat(String). The default format is "PNG" because JPEG is
 * lossy and makes generated images look bad and GIF has patent issues.
 * 
 * @author Jonathan Locke
 * @author Gili Tzabari
 * @author Johan Compagner
 */
public abstract class DynamicImageResource extends DynamicWebResource
{
	/** The image type */
	private String format = "png";

	/** The last modified time of this resource */
	private Time lastModifiedTime;

	/**
	 * Creates a dynamic image resource.
	 */
	public DynamicImageResource()
	{
	}

	/**
	 * Creates a dynamic resource from for the given locale
	 * 
	 * @param locale
	 *            The locale of this resource
	 */
	public DynamicImageResource(Locale locale)
	{
		super(locale);
	}

	/**
	 * Creates a dynamic resource from for the given locale
	 * 
	 * @param format
	 *            The image format ("png", "jpeg", etc)
	 */
	public DynamicImageResource(String format)
	{
		setFormat(format);
	}

	/**
	 * Creates a dynamic resource from for the given locale
	 * 
	 * @param format
	 *            The image format ("png", "jpeg", etc)
	 * @param locale
	 *            The locale of this resource
	 */
	public DynamicImageResource(String format, Locale locale)
	{
		super(locale);
		setFormat(format);
	}

	/**
	 * @return Returns the image format.
	 */
	public synchronized final String getFormat()
	{
		return format;
	}

	/**
	 * Sets the format of this resource
	 * 
	 * @param format
	 *            The format (jpg, png or gif..)
	 */
	public synchronized final void setFormat(String format)
	{
		this.format = format;
	}

	/**
	 * set the last modified time for this resource.
	 * 
	 * @param time
	 */
	protected synchronized void setLastModifiedTime(Time time)
	{
		lastModifiedTime = time;
	}

	/**
	 * @param image
	 *            The image to turn into data
	 * @return The image data for this dynamic image
	 */
	protected byte[] toImageData(final BufferedImage image)
	{
		try
		{
			// Create output stream
			final ByteArrayOutputStream out = new ByteArrayOutputStream();

			// Get image writer for format
			final ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();

			// Write out image
			writer.setOutput(ImageIO.createImageOutputStream(out));
			writer.write(image);

			// Return the image data
			return out.toByteArray();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to convert dynamic image to stream", e);
		}
	}

	/**
	 * @see DynamicWebResource#getResourceState()
	 */
	@Override
	protected synchronized ResourceState getResourceState()
	{
		return new ResourceState()
		{
			private byte[] imageData;
			private final String contentType = "image/" + format;

			@Override
			public Time lastModifiedTime()
			{
				if (lastModifiedTime == null)
				{
					lastModifiedTime = DynamicImageResource.this.lastModifiedTime;
					if (lastModifiedTime == null)
					{
						lastModifiedTime = Time.now();
					}
				}
				return lastModifiedTime;
			}

			@Override
			public byte[] getData()
			{
				if (imageData == null)
				{
					imageData = getImageData();
				}
				return imageData;
			}

			@Override
			public String getContentType()
			{
				return contentType;
			}
		};
	}

	/**
	 * Get image data for our dynamic image resource. If the subclass
	 * regenerates the data, it should set the lastModifiedTime when it does so.
	 * This ensures that image caching works correctly.
	 * 
	 * @return The image data for this dynamic image
	 */
	protected abstract byte[] getImageData();
}
