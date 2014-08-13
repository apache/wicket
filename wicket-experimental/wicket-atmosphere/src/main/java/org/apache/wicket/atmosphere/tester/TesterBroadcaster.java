package org.apache.wicket.atmosphere.tester;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Entry;
import org.atmosphere.util.SimpleBroadcaster;

/**
 *
 */
class TesterBroadcaster extends SimpleBroadcaster
{
	public AtmosphereConfig getApplicationConfig()
	{
		return config;
	}

	@Override
	protected void executeBlockingWrite(AtmosphereResource resource, Entry entry)
	{
		AtmosphereResponse response = resource.getResponse();
		String message = entry.message.toString();
		response.write(message);
	}
}
