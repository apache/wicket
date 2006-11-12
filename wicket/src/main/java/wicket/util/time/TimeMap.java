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
package wicket.util.time;

import java.util.HashMap;
import java.util.Map;

/**
 * This class maps ITimeFrames to Objects. Since values are stored using
 * ITimeFrameSource implementing objects, the value returned by the source may
 * vary over time. For example, one implementation of ITimeFrameSource might
 * return the start and end time of lunch on any given day.
 * <p>
 * To associate an object with a dynamic TimeFrame (via ITimeFrameSource), call
 * put(ITimeFrameSource, Object). You can later retrieve the first object for a
 * point in time with get(Time). The method get() is provided for convenience
 * and is equivalent to get(Time.now()).
 * <p>
 * This class is not threadsafe.
 * 
 * @author Jonathan Locke
 */
public final class TimeMap
{
	/** Map from ITimeFrameSource implementing objects to Object values. */
	private final Map<ITimeFrameSource, Object> sources = new HashMap<ITimeFrameSource, Object>();

	/**
	 * @return Object for the current time
	 */
	public Object get()
	{
		return get(Time.now());
	}

	/**
	 * @param time
	 *            The time
	 * @return Gets an Object for the given time value or null if none exists
	 */
	public Object get(final Time time)
	{
		for (ITimeFrameSource timeFrameSource : sources.keySet())
		{
			final TimeFrame current = timeFrameSource.getTimeFrame();
			if (current.contains(time))
			{
				return sources.get(current);
			}
		}

		return null;
	}

	/**
	 * Associates an object with a dynamic time frame
	 * 
	 * @param source
	 *            A source that can produce a timeframe to compare a time value
	 *            with
	 * @param o
	 *            The object to be returned for the given dynamic timeframe
	 */
	public void put(final ITimeFrameSource source, final Object o)
	{
		final TimeFrame timeframe = source.getTimeFrame();

		for (ITimeFrameSource timeFrameSource : sources.keySet())
		{
			final TimeFrame current = timeFrameSource.getTimeFrame();

			if (timeframe.overlaps(current))
			{
				throw new IllegalArgumentException("Timeframe " + timeframe
						+ " overlaps timeframe " + current);
			}
		}

		sources.put(source, o);
	}
}
