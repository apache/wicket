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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import wicket.WicketRuntimeException;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;
import wicket.util.time.Time;

/**
 * An image subclass that allows easy rendering of dynamic images. An image can
 * be set by calling toImageData(BufferedImage) or by implementing getImageData(),
 * and its format can be specified with setFormat(String).
 * After this, the image will be cached as an input stream
 * and will render as would any other Image resource.
 * 
 * @author Jonathan Locke
 */
public abstract class DynamicImageResource extends ImageResource
{
	/** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	/** The image type */
	private String format = "png";
	
	/**
	 * @return Returns the image format.
	 */
	public final String getFormat()
	{
		return format;
	}
	
	/**
	 * Sets the format of this dynamic image, such as "jpeg" or "gif"
	 * 
	 * @param format
	 *            The image format to set.
	 */
	public void setFormat(String format)
	{
		this.format = format;
	}

	/**
	 * @return The image data for this dynamic image
	 */
	protected abstract byte[] getImageData();
	
	/**
	 * @param image The image to turn into data
	 * @return The image data for this dynamic image
	 */
	protected byte[] toImageData(final BufferedImage image)
	{
		try
		{
			// Create output stream
			final ByteArrayOutputStream out = new ByteArrayOutputStream();

			// Get image writer for format
			final ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName(format)
					.next();

			// Write out gif
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
	 * @return Gets the image resource to attach to the component.
	 */
	public IResource getResource()
	{
		return new IResource()
		{
			private InputStream inputStream = null;

			public void close() throws IOException
			{
				if (inputStream != null)
                {
					inputStream.close();
					inputStream = null;
                }
			}

			public String getContentType()
			{
				return "image/" + format;
			}

			public InputStream getInputStream() throws ResourceNotFoundException
			{
				if (inputStream == null)
				{
					inputStream = new ByteArrayInputStream(getImageData());
				}
				return inputStream;
			}

			public Time lastModifiedTime()
			{
				return Time.now();
			}
		};
	}
}


