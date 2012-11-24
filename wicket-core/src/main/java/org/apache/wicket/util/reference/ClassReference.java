package org.apache.wicket.util.reference;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import org.apache.wicket.Application;

/**
 * A serialization-safe reference to a {@link Class}
 * 
 * @author igor
 * 
 * @param <T>
 *            type of class
 */
public class ClassReference<T> implements Serializable
{
	private transient WeakReference<Class<? extends T>> cache;
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param clazz
	 */
	public ClassReference(Class<? extends T> clazz)
	{
		name = clazz.getName();
		cache(clazz);
	}

	/**
	 * @return the {@link Class} stored in this reference
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends T> get()
	{
		Class<? extends T> clazz = cache != null ? cache.get() : null;
		if (clazz == null)
		{
			try
			{
				clazz = (Class<? extends T>)Application.get()
					.getApplicationSettings()
					.getClassResolver()
					.resolveClass(name);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException("Could not resolve class: " + name, e);
			}
			cache(clazz);
		}
		return clazz;
	}

	private void cache(Class<? extends T> clazz)
	{
		cache = new WeakReference<Class<? extends T>>(clazz);
	}

	/**
	 * Diamond operator factory
	 * 
	 * @param clazz
	 * @return class reference
	 */
	public static <T> ClassReference<T> of(Class<T> clazz)
	{
		return new ClassReference<T>(clazz);
	}
}