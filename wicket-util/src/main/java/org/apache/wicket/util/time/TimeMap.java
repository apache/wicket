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
package org.apache.wicket.util.time;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class maps <code>ITimeFrame</code>s to <code>Object</code>s. Since values are stored using
 * <code>ITimeFrameSource</code> implementing objects, the value returned by the source may vary
 * over time. For example, one implementation of <code>ITimeFrameSource</code> might return the
 * start and end time of lunch on any given day.
 * <p>
 * To associate an object with a dynamic <code>TimeFrame</code> (via <code>ITimeFrameSource</code>),
 * call <code>put(ITimeFrameSource, Object)</code>. You can later retrieve the first object for a
 * point in time with <code>get(Time)</code>. The <code>get</code> method is provided for
 * convenience and is equivalent to <code>get(Time.now())</code>.
 * <p>
 * This class is not thread-safe.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public final class TimeMap implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * <code>Map</code> from <code>ITimeFrameSource</code> implementing objects to
	 * <code>Object</code> values.
	 */
	private final Map<ITimeFrameSource, Object> sources = new ConcurrentHashMap<>();

	/**
	 * Retrieves an <code>Object</code> for the current <code>Time</code> value.
	 * 
	 * @return <code>Object</code> for the current <code>Time</code> value
	 */
	public Object get()
	{
		return get(Time.now());
	}

	/**
	 * Retrieves an <code>Object</code> for the given <code>Time</code> value.
	 * 
	 * @param time
	 *            the <code>Time</code> value
	 * @return gets an <code>Object</code> for the given <code>Time</code> value or
	 *         <code>null</code> if none exists
	 */
	public Object get(final Time time)
	{
		for (ITimeFrameSource source : sources.keySet())
		{
			final TimeFrame current = source.getTimeFrame();
			if (current.contains(time))
			{
				return sources.get(current);
			}
		}

		return null;
	}

	/**
	 * Associates an <code>Object</code> with a dynamic <code>TimeFrame</code>.
	 * 
	 * @param source
	 *            a source that can produce a <code>TimeFrame</code> with which to compare a
	 *            <code>Time</code> value
	 * @param o
	 *            the <code>Object</code> to be returned for the given dynamic
	 *            <code>TimeFrame</code>
	 */
	public void put(final ITimeFrameSource source, final Object o)
	{
		final TimeFrame timeframe = source.getTimeFrame();

		for (ITimeFrameSource tfs : sources.keySet())
		{
			final TimeFrame current = tfs.getTimeFrame();

			if (timeframe.overlaps(current))
			{
				throw new IllegalArgumentException("Timeframe " + timeframe +
					" overlaps timeframe " + current);
			}
		}

		sources.put(source, o);
	}
}
