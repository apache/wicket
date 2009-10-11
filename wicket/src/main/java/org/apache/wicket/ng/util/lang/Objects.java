package org.apache.wicket.ng.util.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.ThreadContext;
import org.apache.wicket.ng.util.io.IObjectStreamFactory;
import org.apache.wicket.ng.util.io.IObjectStreamFactory.DefaultObjectStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Objects {

	/**
	 * Returns true if a and b are equal. Either object may be null.
	 * 
	 * @param a
	 *            Object a
	 * @param b
	 *            Object b
	 * @return True if the objects are equal
	 */
	public static boolean equal(final Object a, final Object b)
	{
		if (a == b)
		{
			return true;
		}

		if ((a != null) && (b != null) && a.equals(b))
		{
			return true;
		}

		return false;
	}

	/**
	 * returns hashcode of the objects by calling obj.hashcode(). safe to use when obj is null.
	 * 
	 * @param obj
	 * @return hashcode of the object or 0 if obj is null
	 */
	public static int hashCode(final Object... obj)
	{
		if (obj == null || obj.length == 0)
		{
			return 0;
		}
		int result = 37;
		for (int i = obj.length - 1; i > -1; i--)
		{
			result = 37 * result + (obj[i] != null ? obj[i].hashCode() : 0);
		}
		return result;
	}

	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 * @return The serialized object
	 */
	public static byte[] objectToByteArray(final Object object)
	{
		String applicationName = Application.exists() ? Application.get().getName() : null;
		return objectToByteArray(object, applicationName);
	}
	
	/**
	 * Serializes an object into a byte array.
	 * 
	 * @param object
	 *            The object
	 *           
	 * @param application name
	 * 			  The name of application - required when serialization and deserialisation happen
	 *            outside thread in which application thread local is  set
	 *            
	 * @return The serialized object
	 */
	public static byte[] objectToByteArray(final Object object, String applicationName)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try
			{
				oos = objectStreamFactory.newObjectOutputStream(out);
				oos.writeObject(applicationName);
				oos.writeObject(object);
			}
			finally
			{
				if (oos != null)
				{
					oos.close();
				}
				out.close();
			}
			return out.toByteArray();
		}
		catch (Exception e)
		{
			log.error("Error serializing object " + object.getClass() + " [object=" + object + "]",
				e);
		}
		return null;
	}
	
	/**
	 * De-serializes an object from a byte array.
	 * 
	 * @param data
	 *            The serialized object
	 * @return The object
	 */
	public static Object byteArrayToObject(final byte[] data)
	{
		try
		{
			final ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream ois = null;
			boolean unsetApplication = false;
			try
			{
				ois = objectStreamFactory.newObjectInputStream(in);
				String applicationName = (String)ois.readObject();
				if (applicationName != null && !Application.exists())
				{
					Application app = Application.get(applicationName);
					if (app != null)
					{
						app.set();
						unsetApplication = true;
					}
				}
				return ois.readObject();
			}
			finally
			{
				if (unsetApplication)
				{
					ThreadContext.setApplication(null);
				}
				if (ois != null)
				{
					ois.close();
				}
				in.close();
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not deserialize object using `" +
				objectStreamFactory.getClass().getName() + "` object factory", e);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not deserialize object using `" +
				objectStreamFactory.getClass().getName() + "` object factory", e);
		}
	}
	
	/**
	 * The default object stream factory to use. Keep this as a static here opposed to in
	 * Application, as the Application most likely isn't available in the threads we'll be using
	 * this with.
	 */
	private static IObjectStreamFactory objectStreamFactory = new IObjectStreamFactory.DefaultObjectStreamFactory();


	/**
	 * Configure this utility class to use the provided {@link IObjectStreamFactory} instance.
	 * 
	 * @param objectStreamFactory
	 *            The factory instance to use. If you pass in null, the
	 *            {@link DefaultObjectStreamFactory default} will be set (again). Pass null to reset
	 *            to the default.
	 */
	public static void setObjectStreamFactory(IObjectStreamFactory objectStreamFactory)
	{
		if (objectStreamFactory == null)
		{
			Objects.objectStreamFactory = new IObjectStreamFactory.DefaultObjectStreamFactory();
		}
		else
		{
			Objects.objectStreamFactory = objectStreamFactory;
		}
		log.info("using " + Objects.objectStreamFactory + " for creating object streams");
	}
	
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(Objects.class);
}
