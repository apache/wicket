/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import wicket.WicketRuntimeException;
import wicket.util.io.ByteArrayOutputStream;
import wicket.util.io.Streams;
import wicket.util.lang.Bytes;
import wicket.util.lang.AbstractClassClosure;

/**
 * A factory object that constructs JAR resources. You can add data via
 * add(String path, InputStream in), which will copy the given input stream into
 * the JAR under the given path. You can also add the closure of classes
 * referenced by a List of Class objects by calling addClassClosures(List
 * classes). Once the getResource() method has been called, the underlying JAR
 * will be closed and you can no longer add classes or data to it. If you
 * attempt to, an IllegalStateException will be thrown.
 * 
 * @author Jonathan Locke
 */
public class JarResourceFactory
{
	/** The JAR */
	final JarOutputStream jar;

	/** The byte array output stream where the JAR file is being assembled */
	final ByteArrayOutputStream out = new ByteArrayOutputStream();

	/** The byte array resource being constructed by this factory */
	ByteArrayResource resource;

	/**
	 * Constructor
	 */
	public JarResourceFactory()
	{
		try
		{
			jar = new JarOutputStream(out);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to initialize JAR", e);
		}
	}

	/**
	 * Adds a given input stream at a given path in the JAR being constructed by
	 * this JarResourceFactory.
	 * 
	 * @param path
	 *            The resource path
	 * @param in
	 *            The input stream to copy into the JAR
	 */
	public void add(final String path, final InputStream in)
	{
		checkWritable();
		try
		{
			final ZipEntry entry = new ZipEntry(path + ".class");
			jar.putNextEntry(entry);
			Streams.copy(in, jar);
			jar.closeEntry();
			System.out.println("JAR: Added " + path);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to add input " + in + " to jar at path "
					+ path, e);
		}
	}

	/**
	 * Adds the closure of classes referenced in the list to the JAR being
	 * constructed by this JarResourceFactory.
	 * 
	 * @param classes
	 *            Classes to jar the closures of
	 */
	public void addClassClosures(final List classes)
	{
		checkWritable();
		new AbstractClassClosure(classes, false)
		{

			protected void addClass(final String name, final InputStream is)
			{
				add(name.replace('.', '/'), is);
			}
		};
	}

	/**
	 * @return The ByteArrayResource for the JAR constructed by this factory.
	 */
	public ByteArrayResource getResource()
	{
		if (!isClosed())
		{
			close();
		}
		return resource;
	}

	/**
	 * Checks that this resource is not closed already
	 */
	private void checkWritable()
	{
		if (resource != null)
		{
			throw new IllegalStateException("JarResourceFactory already closed");
		}
	}

	/**
	 * Closes this JAR resource, at which point it is prepared for access.
	 */
	private void close()
	{
		checkWritable();
		try
		{
			jar.close();
			out.close();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Unable to close JAR", e);
		}
		System.out.println("JAR: Size is " + Bytes.bytes(out.size()));
		resource = new ByteArrayResource("application/x-compressed", out.toByteArray());
		resource.setCacheable(true);
	}

	/**
	 * @return True if this jar resource factory has already been closed
	 */
	private boolean isClosed()
	{
		return resource != null;
	}
}
