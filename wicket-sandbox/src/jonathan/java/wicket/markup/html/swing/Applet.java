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
package wicket.markup.html.swing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JApplet;
import javax.swing.JPanel;

import wicket.Application;
import wicket.IResourceListener;
import wicket.Resource;
import wicket.SharedResources;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebComponent;
import wicket.markup.html.swing.ClassClosure;
import wicket.model.IModel;
import wicket.resource.ByteArrayResource;
import wicket.util.io.ByteArrayOutputStream;
import wicket.util.io.Streams;

/**
 * This component integrates Swing tightly with Wicket by automatically
 * generating Swing applets on demand. The applet's JAR file is automatically
 * created from the code statically referenced by the Applet.IInitializer
 * interface. Once the JAR file for a given Applet subclass has been created, it
 * is reused for all future instances of the Applet. The auto-created JAR is
 * referenced by an automatically generated APPLET tag. The result of this is
 * that an Applet component creates an applet with virtually no work on the
 * programmer's part beyond populating the JPanel and working with the model,
 * which is automatically proxied to/from the applet.
 * <p>
 * In your IInitializer implementation, you can populate a JPanel with any Swing
 * components that you want. When a significant action occurs such as a form
 * submit, the Applet.updateServer() method can be called and the applet
 * component automatically updates the server side model by using Ajax to ask
 * the applet to send its model back to the server.
 * 
 * @author Jonathan Locke
 */
public abstract class Applet extends WebComponent implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** Root class for applet JAR */
	private Class appletCodeClass;

	/** Extra root classes for applet JAR to handle dynamic loading */
	private List/* <Class> */classes;

	/**
	 * The applet implementation used to host the user's JPanel.
	 * 
	 * @author Jonathan Locke
	 */
	public static class HostApplet extends JApplet
	{
		@Override
		public void init()
		{

		}
	}

	/**
	 * The Applet.IInitializer interface should be implemented by the class
	 * passed to the Applet constructor. This class and every class referenced
	 * by it will be automatically included in the applet JAR file for this
	 * applet. When the applet is loaded by the client browser, the init()
	 * method will be called, passing in a JPanel to populate with components
	 * and the model object produced by the Applet component's IModel.
	 * 
	 * @author Jonathan Locke
	 */
	public static interface IInitializer
	{
		/**
		 * Interface to code that initializes a JPanel using a model.
		 * 
		 * @param panel
		 *            The panel to populate with components
		 * @param model
		 *            The model to update in the applet
		 */
		void init(JPanel panel, Object model);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param appletCodeClass
	 *            The class that implement's this applet
	 */
	public Applet(final String id, final Class appletCodeClass)
	{
		super(id);
		addAppletCodeClass(appletCodeClass);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The wicket model
	 * @param appletCodeClass
	 *            The class that implement's this applet
	 */
	public Applet(final String id, final IModel model, final Class appletCodeClass)
	{
		super(id, model);
		addAppletCodeClass(appletCodeClass);
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
		if (classes == null)
		{
			classes = new ArrayList(1);
		}
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
		final Object model = getModelObject();
		final byte[] bytes = objectToByteArray(model);
		final ByteArrayResource resource = new ByteArrayResource("application/x-wicket-model", bytes);
		resource.onResourceRequested();
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
		maybeCreateJar();
		checkComponentTag(tag, "applet");
		tag.put("code", HostApplet.class.getName() + ".class");
		tag.put("archive", SharedResources.path(getApplication(), Applet.class, appletCodeClass
				.getName(), null, null));
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
		replaceComponentTagBody(findMarkupStream(), tag, "<param name=\"component\" value=\""
				+ urlFor(IResourceListener.class) + "\"/>");
		super.onComponentTag(tag);
	}

	/**
	 * @param appletCodeClass
	 */
	private void addAppletCodeClass(final Class appletCodeClass)
	{
		// Applet code must implement IAppletCode interface
		if (!appletCodeClass.isAssignableFrom(IInitializer.class))
		{
			throw new IllegalArgumentException(
					"Applet code class for Applet must implement IAppletCode");
		}
		this.appletCodeClass = appletCodeClass;
	}

	/**
	 * De-serializes an object from a byte array.
	 * 
	 * @param data
	 *            The serialized object
	 * @return The object
	 */
	// FIXME: Belongs in Objects.java 
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
	// FIXME: Belongs in Objects.java 
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
					ZipEntry entry = new ZipEntry(name);
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
			jar.close();
			out.close();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
		return out.toByteArray();
	}

	/**
	 * Possibly create a jar file using the specified root classes
	 */
	private void maybeCreateJar()
	{
		// Get shared resources for this application
		final SharedResources resources = Application.get().getSharedResources();

		// See if resource is already registered
		Resource resource = resources.get(Applet.class, appletCodeClass.getName(), null, null,
				false);

		// If no JAR resource yet generated
		if (resource == null)
		{
			// Create JAR resource
			resource = new ByteArrayResource("jar", jarClasses(classes));

			// Add to application shared resources
			resources.add(Applet.class, appletCodeClass.getName(), null, null, resource);
		}
	}
}
