package org.apache.wicket.ng.util.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Classes
{

	/**
	 * @param <T>
	 *            class type
	 * @param className
	 *            Class to resolve
	 * @return Resolved class
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> resolveClass(final String className)
	{
		if (className == null)
		{
			return null;
		}
		try
		{
			// TODO: Ask Application
//			if (Application.exists())
//			{
//				return (Class<T>)Application.get()
//					.getApplicationSettings()
//					.getClassResolver()
//					.resolveClass(className);
//			}
			return (Class<T>)Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			log.warn("Could not resolve class: " + className);
			return null;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(Classes.class);

}
