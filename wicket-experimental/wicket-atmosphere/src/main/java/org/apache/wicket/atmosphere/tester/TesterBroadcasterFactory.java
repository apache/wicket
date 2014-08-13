package org.apache.wicket.atmosphere.tester;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.atmosphere.cpr.DefaultBroadcasterFactory;
import org.atmosphere.util.SimpleBroadcaster;

/**
*
*/
class TesterBroadcasterFactory extends DefaultBroadcasterFactory
{
	private final TesterBroadcaster singleBroadcaster;

	TesterBroadcasterFactory(AtmosphereConfig c, TesterBroadcaster broadcaster)
	{
		super(SimpleBroadcaster.class, BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.NEVER.name(), c);

		this.singleBroadcaster = broadcaster;

		// expose myself as BroadcasterFactory.getDefault();
		factory = this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Broadcaster> T lookup(Class<T> c, Object id, boolean createIfNull, boolean unique)
	{
		return (T) singleBroadcaster;
	}
}
