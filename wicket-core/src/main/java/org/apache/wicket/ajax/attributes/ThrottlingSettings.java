package org.apache.wicket.ajax.attributes;

import org.apache.wicket.IClusterable;
import org.apache.wicket.util.time.Duration;

/**
 * Class to keep track of throttling settings.
 *
 * @author ivaynberg
 */
public class ThrottlingSettings implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final Duration delay;
	private final String id;

	/**
	 * If it is set to true, then the timer is reset each time the throttle function
	 * gets called. Use this behaviour if you want something to happen at X milliseconds
	 * after the *last* call to throttle. If the parameter is not set, or set to false,
	 * then the timer is not reset.
	 */
	private final boolean postponeTimerOnUpdate;

	/**
	 * Construct.
	 *
	 * @param id    throttle id
	 * @param delay throttle delay
	 */
	public ThrottlingSettings(final String id, final Duration delay)
	{
		this(id, delay, false);
	}

	/**
	 * Construct.
	 *
	 * @param id    throttle id
	 * @param delay throttle delay
	 * @param postponeTimerOnUpdate postpone timer
	 */
	public ThrottlingSettings(final String id, final Duration delay, final boolean postponeTimerOnUpdate)
	{
		this.id = id;
		this.delay = delay;
		this.postponeTimerOnUpdate = postponeTimerOnUpdate;
	}

	/**
	 * @return throttle delay
	 */
	public Duration getDelay()
	{
		return delay;
	}

	/**
	 * @return throttle id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @return
	 */
	public boolean getPostponeTimerOnUpdate() {
		return postponeTimerOnUpdate;
	}
}