/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.time;

/**
 * Immutable class which represents an interval of time with a beginning and an
 * end. The beginning value is inclusive and the end value is exclusive. In
 * other words, the time frame of 1pm to 2pm includes 1pm, but not 2pm. 1:59:59
 * is the last value in the timeframe.
 * <p>
 * TimeFrames can be constructed by calling the valueOf static factory methods
 * valueOf(Time, Time) (yielding a TimeFrame between two absolute times) and
 * valueOf(Time, Duration) yielding a TimeFrame starting at an absolute time and
 * having a given length.
 * <p>
 * The start and end of a TimeFrame can be retrieved by calling getStart() and
 * getEnd(). Its duration can be retrieved by calling getDuration().
 * <p>
 * The contains(Time) method can be called to determine if a TimeFrame contains
 * a given point in time. The overlaps(TimeFrame) method can be called to
 * determine if two TimeFrames overlap.
 * <p>
 * The eachDay(TimeOfDay, TimeOfDay) will return a TimeFrameSource which
 * generates a timeframe using the two times of day. In other words, if the
 * start is 3pm and the end is 4pm, the TimeFrameSource returned will yield
 * 3-4pm on the day it is called (each day).
 * 
 * @author Jonathan Locke
 */
public final class TimeFrame implements ITimeFrameSource
{
	/** End of this timeframe */
	private final Time end;
	
	/** Begining of this timeframe */
	private final Time start;

	/**
	 * Returns a timeframe source for a start and end time-of-day. For example,
	 * called with 3pm and 5pm as parameters, the timeframe source returned
	 * would produce timeframe objects representing 3pm-5pm on whatever day it
	 * is when the caller calls the timeframesource interface.
	 * 
	 * @param startTimeOfDay
	 *            The start time for this time frame each day
	 * @param endTimeOfDay
	 *            The end time for this time frame each day
	 * @return A timeframe source which will return the specified timeframe each
	 *         day
	 */
	public static ITimeFrameSource eachDay(final TimeOfDay startTimeOfDay,
			final TimeOfDay endTimeOfDay)
	{
		check(startTimeOfDay, endTimeOfDay);

		return new ITimeFrameSource()
		{
			public TimeFrame getTimeFrame()
			{
				return new TimeFrame(Time.valueOf(startTimeOfDay), Time.valueOf(endTimeOfDay));
			}
		};
	}

	/**
	 * Creates a time frame for a start and duration
	 * 
	 * @param start
	 *            The start time
	 * @param duration
	 *            The duration
	 * @return The time frame
	 * @throws IllegalArgumentException
	 *             Thrown if start time is before end time
	 */
	public static TimeFrame valueOf(final Time start, final Duration duration)
	{
		return new TimeFrame(start, start.add(duration));
	}

	/**
	 * Creates a time frame for a start and end time
	 * 
	 * @param start
	 *            The start time
	 * @param end
	 *            The end time
	 * @return The time frame
	 * @throws IllegalArgumentException
	 *             Thrown if start time is before end time
	 */
	public static TimeFrame valueOf(final Time start, final Time end)
	{
		return new TimeFrame(start, end);
	}

	/**
	 * Checks consistency of start and end values, ensuring that the end value
	 * is less than the start value.
	 * 
	 * @param start
	 *            Start value
	 * @param end
	 *            End value
	 * @throws IllegalArgumentException
	 *             Thrown if end is less than start
	 */
	private static void check(final AbstractTimeValue start, final AbstractTimeValue end)
	{
		// Throw illegal argument exception if end is less than start
		if (end.lessThan(start))
		{
			throw new IllegalArgumentException("Start time of time frame " + start
					+ " was after end time " + end);
		}
	}

	/**
	 * Private constructor to force use of static factory methods
	 * 
	 * @param start
	 *            The start time
	 * @param end
	 *            The end time
	 * @throws IllegalArgumentException
	 *             Thrown if start time is before end time
	 */
	private TimeFrame(final Time start, final Time end)
	{
		check(start, end);
		this.start = start;
		this.end = end;
	}

	/**
	 * @param time
	 *            The time to check
	 * @return True if this time frame contains the given time
	 */
	public boolean contains(final Time time)
	{
		return (start.equals(time) || start.before(time)) && end.after(time);
	}

	/**
	 * @return The duration of this time frame
	 */
	public Duration getDuration()
	{
		return end.subtract(start);
	}

	/**
	 * @return The end of this time frame
	 */
	public Time getEnd()
	{
		return end;
	}

	/**
	 * @return The start of this time frame
	 */
	public Time getStart()
	{
		return start;
	}

	/**
	 * Implementation of ITimeFrameSource that simply returns this timeframe
	 * 
	 * @return Gets this timeframe
	 */
	public TimeFrame getTimeFrame()
	{
		return this;
	}

	/**
	 * @param timeframe
	 *            The timeframe to test
	 * @return True if the given timeframe overlaps this one
	 */
	public boolean overlaps(final TimeFrame timeframe)
	{
		return contains(timeframe.start) || contains(timeframe.end) || timeframe.contains(start)
				|| timeframe.contains(end);
	}

	/**
	 * @return String representation of this object
	 */
	public String toString()
	{
		return "[start=" + start + ", end=" + end + "]";
	}
}
