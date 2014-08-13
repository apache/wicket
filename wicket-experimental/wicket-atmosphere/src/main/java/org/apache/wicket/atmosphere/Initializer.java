package org.apache.wicket.atmosphere;

import de.agilecoders.wicket.webjars.WicketWebjars;
import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;

/**
 * Initializes Wicket Atmosphere application.
 * Installs Webjars support.
 */
public class Initializer implements IInitializer
{
	@Override
	public void init(Application application)
	{
		WicketWebjars.settings();
		WicketWebjars.install((org.apache.wicket.protocol.http.WebApplication) application);
	}

	@Override
	public void destroy(Application application)
	{
	}
}
