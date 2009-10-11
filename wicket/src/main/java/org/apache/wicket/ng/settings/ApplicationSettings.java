package org.apache.wicket.ng.settings;

import org.apache.wicket.ng.application.ClassResolver;

public interface ApplicationSettings
{
	/**
	 * Gets the default resolver to use when finding classes and resources
	 * 
	 * @return Default class resolver
	 */
	ClassResolver getClassResolver();
	
	/**
	 * Sets the default class resolver to use when finding classes and resources
	 * 
	 * @param defaultClassResolver
	 *            The default class resolver
	 */
	void setClassResolver(final ClassResolver defaultClassResolver);
}
