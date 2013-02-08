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
package org.apache.wicket.ajax.attributes;

import org.apache.wicket.util.io.IClusterable;
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
	 * If it is set to true, then the timer is reset each time the throttle function
	 * gets called. Use this behaviour if you want something to happen at X milliseconds
	 * after the *last* call to throttle. If the parameter is not set, or set to false,
	 * then the timer is not reset.
	 */
	public boolean getPostponeTimerOnUpdate() {
		return postponeTimerOnUpdate;
	}
}