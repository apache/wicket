/*
 * $Id: StringBufferResourceStream.java 3307 2005-11-30 15:57:34 -0800 (Wed, 30
 * Nov 2005) ivaynberg $ $Revision: 3307 $ $Date: 2005-11-30 15:57:34 -0800
 * (Wed, 30 Nov 2005) $
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
package wicket.util.resource;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.io.Streams;
import wicket.util.lang.Packages;
import wicket.util.resource.locator.ClassLoaderResourceStreamLocator;
import wicket.util.string.interpolator.MapVariableInterpolator;

/**
 * A string resource that can be appended to.
 * 
 * @author Eelco Hillenius
 */
// TODO cache templates application scoped with a watch
public class PackagedTextTemplate extends TextTemplate
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(PackagedTextTemplate.class);

	/** class loader stream locator. */
	private static final ClassLoaderResourceStreamLocator streamLocator = new ClassLoaderResourceStreamLocator();

	/** contents */
	private StringBuffer buffer = new StringBuffer();

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            The class to be used for retrieving the classloader for
	 *            loading the packaged template.
	 * @param fileName
	 *            The name of the file, relative to the clazz position
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName)
	{
		this(clazz, fileName, "text");
	}

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            The class to be used for retrieving the classloader for
	 *            loading the packaged template.
	 * @param fileName
	 *            the name of the file, relative to the clazz position
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or
	 *            "text/html"
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName, final String contentType)
	{
		this(clazz, fileName, contentType, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param clazz
	 *            The class to be used for retrieving the classloader for
	 *            loading the packaged template.
	 * @param fileName
	 *            the name of the file, relative to the clazz position
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or
	 *            "text/html"
	 * @param encoding
	 *            The file's encoding, e.g. 'UTF-8'
	 */
	public PackagedTextTemplate(final Class clazz, final String fileName, final String contentType,
			final String encoding)
	{
		super(contentType);

		String path = Packages.absolutePath(clazz, fileName);
		IResourceStream stream = streamLocator.locate(clazz, path);

		if (stream == null)
		{
			throw new IllegalArgumentException("resource " + fileName + " not found for scope "
					+ clazz + " (path = " + path + ")");
		}

		try
		{
			if (encoding != null)
			{
				buffer.append(Streams.readString(stream.getInputStream(), encoding));
			}
			else
			{
				buffer.append(Streams.readString(stream.getInputStream()));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Interpolate the map of variables with the content and replace the content
	 * with the result. Variables are denoted in this string by the syntax
	 * ${variableName}. The contents will be altered by replacing each variable
	 * of the form ${variableName} with the value returned by
	 * variables.getValue("variableName").
	 * <p>
	 * WARNING there is no going back to the original contents after the
	 * interpolation is done. if you need to do different interpolations on the
	 * same original contents, use method {@link #asString(Map)} instead.
	 * </p>
	 * 
	 * @param variables
	 *            The variables to interpolate
	 * @return This for chaining
	 */
	public final PackagedTextTemplate interpolate(Map variables)
	{
		if (variables != null)
		{
			String result = new MapVariableInterpolator(buffer.toString(), variables).toString();
			buffer.delete(0, buffer.length());
			buffer.append(result);
		}
		return this;
	}

	/**
	 * @see wicket.util.resource.IResourceStream#length()
	 */
	public final long length()
	{
		return buffer.length();
	}

	/**
	 * @see wicket.util.resource.AbstractStringResourceStream#getString()
	 */
	@Override
	public String getString()
	{
		return buffer.toString();
	}
}
