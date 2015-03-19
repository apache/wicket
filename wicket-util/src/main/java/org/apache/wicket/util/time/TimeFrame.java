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

import org.apache.wicket.util.lang.Objects;

/**
 * Immutable class which represents an interval of time with a beginning and an end. The beginning
 * value is inclusive and the end value is exclusive. In other words, the time frame of 1pm to 2pm
 * includes 1pm, but not 2pm. 1:59:59 is the last value in the <code>TimeFrame</code>.
 * <p>
 * <code>TimeFrame</code>s can be constructed by calling the <code>valueOf</code> static factory
 * methods <code>valueOf(Time, Time)</code> (yielding a <code>TimeFrame</code> between two absolute
 * times) and <code>valueOf(Time, Duration)</code> yielding a <code>TimeFrame</code> starting at an
 * absolute time and having a given length.
 * <p>
 * The start and end of a <code>TimeFrame</code> can be retrieved by calling <code>getStart</code>
 * and <code>getEnd</code>. Its duration can be retrieved by calling <code>getDuration</code>.
 * <p>
 * The <code>contains(Time)</code> method can be called to determine if a <code>TimeFrame</code>
 * contains a given point in time. The <code>overlaps(TimeFrame)</code> method can be called to
 * determine if two <code>TimeFrames</code> overlap.
 * <p>
 * The <code>eachDay(TimeOfDay, TimeOfDay)</code> will return a <code>TimeFrameSource</code> which
 * generates a <code>TimeFrame</code> using the two times of day. In other words, if the start is
 * 3pm and the end is 4pm, the <code>TimeFrameSource</code> returned will yield 3-4pm on the day it
 * is called (each day).
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public final class TimeFrame implements ITimeFrameSource
{
	private static final long serialVersionUID = 1L;

	/** end of this <code>TimeFrame</code> */
	private final Time end;

	/** beginning of this <code>TimeFrame</code> */
	private final Time start;

	/**
	 * Creates an <code>ITimeFrameSource</code> source for start and end <code>TimeOfDay</code>s.
	 * For example, called with 3pm and 5pm as parameters, the <code>TimeFrame</code> source
	 * returned would produce <code>TimeFrame</code> objects representing 3pm-5pm on whatever day it
	 * is when the caller calls the <code>TimeFrameSource</code> interface.
	 * 
	 * @param startTimeOfDay
	 *            the start <code>TimeOfDay</code> for this <code>TimeFrame</code> each day
	 * @param endTimeOfDay
	 *            the end <code>TimeOfDay</code> for this <code>TimeFrame</code> each day
	 * @return a <code>TimeFrameSource</code> which will return the specified <code>TimeFrame</code>
	 *         each day
	 */
	public static ITimeFrameSource eachDay(final TimeOfDay startTimeOfDay,
		final TimeOfDay endTimeOfDay)
	{
		check(startTimeOfDay, endTimeOfDay);

		return new ITimeFrameSource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public TimeFrame getTimeFrame()
			{
				return new TimeFrame(Time.valueOf(startTimeOfDay), Time.valueOf(endTimeOfDay));
			}
		};
	}

	/**
	 * Creates a <code>TimeFrame</code> for a start <code>Time</code> and <code>Duration</code>.
	 * 
	 * @param start
	 *            the start <code>Time</code>
	 * @param duration
	 *            the <code>Duration</code>
	 * @return the <code>TimeFrame</code>
	 * @throws IllegalArgumentException
	 *             thrown if start <code>Time</code> value is before end <code>Time</code> value
	 */
	public static TimeFrame valueOf(final Time start, final Duration duration)
	{
		return new TimeFrame(start, start.add(duration));
	}

	/**
	 * Creates a <code>TimeFrame</code> for given start and end <code>Time</code>s.
	 * 
	 * @param start
	 *            the start <code>Time</code>
	 * @param end
	 *            the end <code>Time</code>
	 * @return the <code>TimeFrame</code>
	 * @throws IllegalArgumentException
	 *             thrown if start <code>Time</code> value is before end <code>Time</code> value
	 */
	public static TimeFrame valueOf(final Time start, final Time end)
	{
		return new TimeFrame(start, end);
	}

	/**
	 * Checks consistency of start and end <code>AbstractTimeValue</code> values, ensuring that the
	 * end value is less than the start value.
	 * 
	 * @param start
	 *            start <code>AbstractTimeValue</code> value
	 * @param end
	 *            end <code>AbstractTimeValue</code> value
	 * @throws IllegalArgumentException
	 *             thrown if end is less than start
	 */
	private static void check(final AbstractTimeValue start, final AbstractTimeValue end)
	{
		// Throw illegal argument exception if end is less than start
		if (end.lessThan(start))
		{
			throw new IllegalArgumentException("Start time of time frame " + start +
				" was after end time " + end);
		}
	}

	/**
	 * Private constructor to force use of static factory methods.
	 * 
	 * @param start
	 *            the start <code>Time</code>
	 * @param end
	 *            the end <code>Time</code>
	 * @throws IllegalArgumentException
	 *             thrown if start <code>Time</code> value is before end <code>Time</code> value
	 */
	private TimeFrame(final Time start, final Time end)
	{
		check(start, end);
		this.start = start;
		this.end = end;
	}

	/**
	 * Determines if this <code>TimeFrame</code> contains a given point in time.
	 * 
	 * @param time
	 *            the <code>Time</code> to check
	 * @return <code>true</code> if this <code>TimeFrame</code> contains the given time
	 */
	public boolean contains(final Time time)
	{
		return (start.equals(time) || start.before(time)) && end.after(time);
	}

	/**
	 * Retrieves the <code>Duration</code> of this <code>TimeFrame</code>.
	 * 
	 * @return the <code>Duration</code> of this <code>TimeFrame</code>
	 */
	public Duration getDuration()
	{
		return end.subtract(start);
	}

	/**
	 * Retrieves the end <code>Time</code> of this <code>TimeFrame</code>.
	 * 
	 * @return the end of this <code>TimeFrame</code>
	 */
	public Time getEnd()
	{
		return end;
	}

	/**
	 * Retrieves the start <code>Time</code> of this <code>TimeFrame</code>.
	 * 
	 * @return the start of this <code>TimeFrame</code>
	 */
	public Time getStart()
	{
		return start;
	}

	/**
	 * Implementation of <code>ITimeFrameSource</code> that simply returns this
	 * <code>TimeFrame</code>.
	 * 
	 * @return this <code>TimeFrame</code>
	 */
	@Override
	public TimeFrame getTimeFrame()
	{
		return this;
	}

	/**
	 * Determines if two <code>TimeFrame</code>s overlap.
	 * 
	 * @param timeframe
	 *            the <code>TimeFrame</code> to test
	 * @return <code>true</code> if the given <code>TimeFrame</code> overlaps this one
	 */
	public boolean overlaps(final TimeFrame timeframe)
	{
		return contains(timeframe.start) || contains(timeframe.end) || timeframe.contains(start) ||
			timeframe.contains(end);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(start, end);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		TimeFrame other = (TimeFrame)obj;
		return Objects.equal(start, other.start) && Objects.equal(end, other.end);
	}

	/**
	 * Converts this <code>TimeFrame</code> to a <code>String</code> representation.
	 * 
	 * @return a <code>String</code> representation of this object
	 */
	@Override
	public String toString()
	{
		return "[start=" + start + ", end=" + end + "]";
	}
}
