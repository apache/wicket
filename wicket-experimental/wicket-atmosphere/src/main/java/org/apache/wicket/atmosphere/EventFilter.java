package org.apache.wicket.atmosphere;

import com.google.common.base.Predicate;

/**
 * Used by {@link EventBus} to filters subscriptions for a given event. Both event type and
 * {@linkplain Subscribe#filter() subscription filter} are taken into account.
 * 
 * @author papegaaij
 */
public class EventFilter implements Predicate<EventSubscription>
{
	private Object event;

	/**
	 * Construct.
	 * 
	 * @param event
	 */
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
