package org.apache.wicket.ajax.effects;

import org.apache.wicket.Component;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * An effect is responsible to render the JavaScript that should be
 * used to show the animation effect.
 */
public class Effect
{
	/**
	 * The default duration of all effects which do not specify
	 * their duration explicitly via {@linkplain #setDuration(org.apache.wicket.util.time.Duration)}.
	 *
	 * <p>Non final so the application can specify</p> its own default
	 */
	public static Duration DEFAULT_DURATION = Duration.milliseconds(300L);

	/**
	 * The name of the effect.
	 */
	private final String name;

	/**
	 * The duration of the animation. In milliseconds
	 */
	private Duration duration;

	/**
	 * A flag indicating whether the animation should suspend
	 * the execution of other Ajax response evaluations.
	 * By default effects notify when they are finished and other
	 * evaluations can be executed.
	 */
	private boolean notify = true;

	/**
	 * Constructor.
	 *
	 * @param name
	 *          The name of the effect
	 */
	protected Effect(String name)
	{
		this(name, DEFAULT_DURATION);
	}

	/**
	 * Constructor.
	 *
	 * @param name
	 *          The name of the effect
	 * @param duration
	 *          The duration of the animation.
	 */
	protected Effect(String name, Duration duration)
	{
		this.name = Args.notEmpty(name, "name");
		this.duration = Args.notNull(duration, "duration");
	}

	public Effect setDuration(Duration duration)
	{
		this.duration = duration;
		return this;
	}

	public Duration getDuration()
	{
		return duration;
	}

	public String getName()
	{
		return name;
	}

	public boolean isNotify()
	{
		return notify;
	}

	public Effect setNotify(boolean notify)
	{
		this.notify = notify;
		return this;
	}

	/**
	 * Constructs JavaScript like: Wicket.Effect['name']('componentMarkupId', duration)
	 *
	 * @param component
	 * @return the JavaScript used to execute the effect
	 */
	public CharSequence toJavaScript(Component component)
	{
		Args.notNull(component, "component");

		StringBuilder js = new StringBuilder();
		if (isNotify())
		{
			js.append("notify|");
		}
		js.append("Wicket.Effect['").append(name).append("']");
		js.append("('").append(component.getMarkupId()).append("', ")
				.append(getDuration().getMilliseconds());
		if (isNotify())
		{
			js.append(", notify");
		}
		js.append(");");

		return js;
	}
}
