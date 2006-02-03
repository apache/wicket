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
package applet;

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
import wicket.markup.html.form.IFormSubmitListener;
import wicket.markup.parser.XmlTag;
import wicket.model.IModel;
import wicket.protocol.http.IMultipartWebRequest;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebResponse;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.resource.ByteArrayResource;
import wicket.util.io.ByteArrayOutputStream;
import wicket.util.io.Streams;
import wicket.util.lang.Bytes;
import wicket.util.string.Strings;
import wicket.util.upload.FileItem;

/**
 * This component integrates Swing tightly with Wicket by automatically
 * generating Swing applets on demand. The applet's JAR file is automatically
 * created from the closure of class files statically referenced by the IApplet-
 * interface-implementing class passed to the Applet constructor. If any classes
 * are dynamically loaded by an applet, they can be added to the Applet JAR by
 * calling Applet.addClass(Class). The IApplet-implementing-class passed to the
 * Applet constructor will only be (automatically) instantiated in the client
 * browser's VM by the host applet container implementation.
 * <p>
 * Once the JAR file for a given Applet subclass has been created, it is reused
 * for all future instances of the Applet. The auto-created JAR is referenced by
 * an automatically modified APPLET tag, resulting in an automatically generated
 * applet. Attributes you set on the APPLET tag in your HTML such as width and
 * height will be left unchanged.
 * <p>
 * To add Swing behavior to an Applet, the user's implementation of IApplet in
 * the class passed to the Applet constructor should populate the Container
 * passed to the IApplet.init() method with Swing components. Those components
 * should edit the model passed into the same init() method. The model can be
 * pushed back to the server at any time (via an internally executed form POST
 * over HTTP) by manually calling the setModel(Object) method on the
 * IAppletServer interface passed to the IApplet.init() method. Such a manual
 * update is not necessary if the Applet component is contained in a Form. For
 * Applets nested within Forms, the form submit will result in an automatic call
 * via JavaScript to the IAppletServer.setModel() method before the Form itself
 * posts. Therefore, the Applet's model will be updated by the time
 * Form.onSubmit() is called. This allows users to augment HTML forms with
 * Applet based Swing components in a modular and reusable fashion.
 * 
 * @author Jonathan Locke
 */
public class Applet extends WebComponent implements IResourceListener, IFormSubmitListener
{
	private static final long serialVersionUID = 1L;

	/** Root class for applet JAR */
	private Class appletClass;

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
	 * @param appletClass
	 *            The class that implement's this applet
	 */
	public Applet(final String id, final Class appletClass)
	{
		super(id);
		addAppletClass(appletClass);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The wicket model
	 * @param appletClass
	 *            The class that implements this applet's initialization
	 */
	public Applet(final String id, final IModel model, final Class appletClass)
	{
		super(id, model);
		addAppletClass(appletClass);
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
	 * Called when model data is posted back from the client
	 */
	public void onFormSubmitted()
	{
		// Get request
		final WebRequest webRequest = ((WebRequest)getRequest()).newMultipartWebRequest(Bytes.MAX);
		getRequestCycle().setRequest(webRequest);

		// Get the item for the path
		final FileItem item = ((IMultipartWebRequest)webRequest).getFile("model");
		try
		{
			final Object model = new ObjectInputStream(item.getInputStream()).readObject();
			System.out.println("Setting model to " + model);
			setAppletModel(model);
		}
		catch (ClassNotFoundException e)
		{
			((WebResponse)getResponse()).setHeader("STATUS", "417");
		}
		catch (IOException e)
		{
			((WebResponse)getResponse()).setHeader("STATUS", "417");
		}
	}

	/**
	 * Sets the model object into this applet
	 * 
	 * @param model
	 *            The model
	 */
	public void setAppletModel(Object model)
	{
		setModelObject(model);
	}

	/**
	 * @return The model for this applet
	 */
	public Object getAppletModel()
	{
		return getModelObject();
	}

	/**
	 * Returns the model for this Applet component as a resource. This enables
	 * the client side HostApplet container to retrieve the model object.
	 * 
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public void onResourceRequested()
	{
		new ByteArrayResource("application/x-wicket-model", objectToByteArray(getAppletModel()))
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
		final String jarName = appletClass.getName() + ".jar";
		final ResourceReference jarResourceReference = new ResourceReference(jarName)
		{
			protected Resource newResource()
			{
				// Create JAR resource
				return new ByteArrayResource("application/x-compressed", jarClasses(classes))
						.setCacheable(false);
			}
		};

		String servletPath = ((ServletWebRequest)getRequest()).getServletPath();
		tag.put("codebase", servletPath + '/'
				+ Strings.beforeLastPathComponent(jarResourceReference.getPath(), '/') + "/");
		tag.put("archive", jarName);
		tag.put("name", getPageRelativePath());
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
		replaceComponentTagBody(markupStream, openTag, "\n<param name=\"getModelUrl\" value=\""
				+ urlFor(IResourceListener.class) + "\"/>"
				+ "\n<param name=\"setModelUrl\" value=\"" + urlFor(IFormSubmitListener.class)
				+ "\"/>" + "\n<param name=\"appletClassName\" value=\"" + appletClass.getName()
				+ "\"/>\n");
	}

	/**
	 * @param appletClass
	 *            The class to add
	 */
	private void addAppletClass(final Class appletClass)
	{
		// Applet code must implement IAppletCode interface
		if (!IApplet.class.isAssignableFrom(appletClass))
		{
			throw new IllegalArgumentException("Applet class " + appletClass.getName()
					+ " must implement " + IApplet.class.getName());
		}
		this.appletClass = appletClass;
		addClass(appletClass);
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
