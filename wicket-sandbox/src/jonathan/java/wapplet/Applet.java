/*
 * $Id$ $Revision:
 * 1.71 $ $Date$
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
package wapplet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import wicket.IResourceListener;
import wicket.Resource;
import wicket.ResourceReference;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;
import wicket.resource.ByteArrayResource;
import wicket.util.io.ByteArrayOutputStream;
import wicket.util.io.Streams;
import wicket.util.lang.Bytes;
import wicket.util.string.Strings;

/**
 * This component integrates Swing tightly with Wicket by automatically
 * generating Swing applets on demand. The applet's JAR file is automatically
 * created from the code statically referenced by the IAppletInitializer
 * interface. Once the JAR file for a given Applet subclass has been created, it
 * is reused for all future instances of the Applet. The auto-created JAR is
 * referenced by an automatically generated APPLET tag. The result of this is
 * that an Applet component creates an applet with virtually no work on the
 * programmer's part beyond populating the JPanel and working with the model,
 * which is automatically proxied to/from the applet.
 * <p>
 * In your IAppletInitializer implementation, you can populate a JPanel with any
 * Swing components that you want. When a significant action occurs such as a
 * form submit, the Applet.updateServer() method can be called and the applet
 * component automatically updates the server side model by using Ajax to ask
 * the applet to send its model back to the server.
 * 
 * @author Jonathan Locke
 */
public class Applet extends WebComponent implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** Root class for applet JAR */
	private Class appletInitializerClass;

	/** Extra root classes for applet JAR to handle dynamic loading */
	private List/* <Class> */classes = new ArrayList(2);

	/**
	 * De-serializes an object from a byte array.
	 * 
	 * @param data
	 *            The serialized object
	 * @return The object
	 */
	// FIXME General: Belongs in Objects.java
	public static Object byteArrayToObject(final byte[] data)
	{
		try
		{
			final ByteArrayInputStream in = new ByteArrayInputStream(data);
			final Object object = new ObjectInputStream(in).readObject();
			in.close();
			return object;
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 * @return The serialized object
	 */
	// FIXME General: Belongs in Objects.java
	public static byte[] objectToByteArray(Object object)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(object);
			out.close();
			return out.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param appletInitializerClass
	 *            The class that implement's this applet
	 */
	public Applet(final String id, final Class appletInitializerClass)
	{
		super(id);
		addAppletIntitializerClass(appletInitializerClass);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The wicket model
	 * @param appletInitializerClass
	 *            The class that implements this applet's initialization
	 */
	public Applet(final String id, final IModel model, final Class appletInitializerClass)
	{
		super(id, model);
		addAppletIntitializerClass(appletInitializerClass);
	}

	/**
	 * Add class root to applet. It should only be necessary to call this method
	 * in the rare case that your applet does dynamic class loading.
	 * 
	 * @param c
	 *            The class to add
	 */
	public void addClass(final Class c)
	{
		classes.add(c);
	}

	/**
	 * Returns the model for this Applet component as a resource. This enables
	 * the client side HostApplet container to retrieve the model object.
	 * 
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested()
	{
		new ByteArrayResource("application/x-wicket-model", objectToByteArray(getModelObject()))
				.onResourceRequested();
	}

	/**
	 * @return Height of applet, or -1 to use default provided in html tag
	 */
	protected int getHeight()
	{
		return -1;
	}

	/**
	 * @return Width of applet, or -1 to use default provided in html tag
	 */
	protected int getWidth()
	{
		return -1;
	}

	/**
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "applet");
		tag.put("code", HostApplet.class.getName());
		final String jarName = appletInitializerClass.getName() + ".jar";
		final ResourceReference jarResourceReference = new ResourceReference(jarName)
		{
			protected Resource newResource()
			{
				// Create JAR resource
				return new ByteArrayResource("application/x-compressed", jarClasses(classes))
						.setCacheable(false);
			}
		};
		tag.put("codebase", "wapplet/"
				+ Strings.beforeLastPathComponent(jarResourceReference.getPath(), '/') + "/");
		tag.put("archive", jarName);
		final int width = getWidth();
		if (width != -1)
		{
			tag.put("width", width);
		}
		final int height = getHeight();
		if (height != -1)
		{
			tag.put("height", height);
		}
		tag.setType(XmlTag.OPEN);
		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, "\n<param name=\"component\" value=\""
				+ urlFor(IResourceListener.class) + "\"/>"
				+ "\n<param name=\"appletInitializerClassName\" value=\""
				+ appletInitializerClass.getName() + "\"/>\n");
	}

	/**
	 * @param appletInitializerClass
	 *            The class to add
	 */
	private void addAppletIntitializerClass(final Class appletInitializerClass)
	{
		// Applet code must implement IAppletCode interface
		if (!IAppletInitializer.class.isAssignableFrom(appletInitializerClass))
		{
			throw new IllegalArgumentException("Applet initializer class "
					+ appletInitializerClass.getName() + " must implement "
					+ IAppletInitializer.class.getName());
		}
		this.appletInitializerClass = appletInitializerClass;
		addClass(appletInitializerClass);
		addClass(HostApplet.class);
	}

	/**
	 * @param classes
	 *            Classes to jar the closures of
	 * @return Jarred classes as a byte[]
	 */
	private byte[] jarClasses(final List classes)
	{
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			final JarOutputStream jar = new JarOutputStream(out);
			new ClassClosure(classes, false)
			{
				protected void addClass(final String name, final InputStream is)
				{
					System.out.println("JAR: Added " + name);
					ZipEntry entry = new ZipEntry(name.replace('.', '/') + ".class");
					try
					{
						jar.putNextEntry(entry);
						Streams.copy(is, jar);
						jar.closeEntry();
					}
					catch (IOException e)
					{
						throw new WicketRuntimeException(e);
					}
				}
			};
			System.out.println("JAR: Size is " + Bytes.bytes(out.size()));
			jar.close();
			out.close();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
		return out.toByteArray();
	}
}
