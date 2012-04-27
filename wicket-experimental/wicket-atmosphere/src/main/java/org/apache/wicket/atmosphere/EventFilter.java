package org.apache.wicket.atmosphere;

import com.google.common.base.Predicate;

public class EventFilter implements Predicate<EventSubscription>
{
	private Object event;

	public EventFilter(Object event)
	{
		this.event = event;
	}

	@Override
	public boolean apply(EventSubscription input)
	{
		return input.getFilter().apply(event);
	}
}
