/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax.effects;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * An effect is responsible to render the JavaScript that should be
 * used to show the animation effect.
 */
public class Effect implements CharSequence
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

	private String componentMarkupId;

	/**
	 * A flag indicating whether the animation should suspend
	 * the execution of other Ajax response evaluations.
	 * By default effects notify when they are finished and other
	 * evaluations can be executed.
	 */
	private boolean notify = false;

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

	public String getComponentMarkupId()
	{
		return componentMarkupId;
	}

	public Effect setComponentMarkupId(String componentMarkupId)
	{
		this.componentMarkupId = componentMarkupId;
		return this;
	}

	/**
	 * Constructs JavaScript like: Wicket.Effect['name']('componentMarkupId', duration)
	 *
	 * @return the JavaScript used to execute the effect
	 */
	private CharSequence toJavaScript()
	{
		StringBuilder js = new StringBuilder();
		if (isNotify())
		{
			js.append("notify|");
		}
		js.append("Wicket.Effect['").append(name).append("']");
		js.append("('").append(getComponentMarkupId()).append("', ")
				.append(getDuration().getMilliseconds());
		if (isNotify())
		{
			js.append(", notify");
		}
		js.append(");");

		return js;
	}

	@Override
	public int length()
	{
		return toJavaScript().length();
	}

	@Override
	public char charAt(int index)
	{
		return toJavaScript().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return toJavaScript().subSequence(start, end);
	}

	@Override
	public String toString()
	{
		return toJavaScript().toString();
	}
}
