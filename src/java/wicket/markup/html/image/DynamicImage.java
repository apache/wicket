/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.image;


import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import wicket.RenderException;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jonathan Locke
 */
public class DynamicImage extends Image
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = 5934721258765771884L;

	private String extension;

    private BufferedImage image;

    private InputStream inputStream;

    private ByteArrayOutputStream out;

    /**
     * Constructor
     * @param name
     */
    public DynamicImage(String name)
    {
        super(name);
    }

    /**
     * @return Returns the extension.
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * @return Returns the image.
     */
    public BufferedImage getImage()
    {
        return image;
    }

    /**
     * @return Returns the inputStream.
     */
    public InputStream getInputStream()
    {
        return inputStream;
    }

    /**
     * @param extension The extension to set.
     * @return This
     */
    public DynamicImage setExtension(String extension)
    {
        this.extension = extension;

        return this;
    }

    /**
     * @param image The image to set
     * @return This
     */
    public DynamicImage setImage(final BufferedImage image)
    {
        try
        {
            out = new ByteArrayOutputStream();

            // Get image writer for extension
            final ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName(extension)
                    .next();

            // Write out gif
            writer.setOutput(ImageIO.createImageOutputStream(out));
            writer.write(image);

            // Set input stream
            setInputStream(new ByteArrayInputStream(out.toByteArray()));
        }
        catch (IOException e)
        {
            throw new RenderException("Unable to convert image to stream", e);
        }

        return this;
    }

    /**
     * @param inputStream Image source input stream
     * @return This
     */
    public DynamicImage setInputStream(final InputStream inputStream)
    {
        this.inputStream = inputStream;

        return this;
    }

    /**
     * @param source The source attribute of the image tag
     * @return Gets the image resource to attach to the component.
     */
    protected IResource getImageResource(final String source)
    {
        return new IResource()
        {
            public void close() throws IOException
            {
                inputStream.close();
            }

            public String getExtension()
            {
                return extension;
            }

            public InputStream getInputStream() throws ResourceNotFoundException
            {
                return inputStream;
            }
        };
    }
}

///////////////////////////////// End of File /////////////////////////////////
