/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.time;

/**
 * Immutable class which represents an interval of time with a beginning and an end. The
 * beginning value is inclusive and the end value is exclusive. In other words, the time
 * frame of 1pm to 2pm includes 1pm, but not 2pm. 1:59:59 is the last value in the
 * timeframe.
 * @author Jonathan Locke
 */
public final class TimeFrame implements ITimeFrameSource
{
    // Start and end points of this time frame
    private final Time start;

    private final Time end;

    /**
     * Private constructor to force use of static factory methods
     * @param start The start time
     * @param end The end time
     * @throws IllegalArgumentException Thrown if start time is before end time
     */
    private TimeFrame(final Time start, final Time end)
    {
        check(start, end);
        this.start = start;
        this.end = end;
    }

    /**
     * @return Gets this timeframe
     */
    public TimeFrame get()
    {
        return this;
    }

    /**
     * Creates a time frame for a start and end time
     * @param start The start time
     * @param end The end time
     * @return The time frame
     * @throws IllegalArgumentException Thrown if start time is before end time
     */
    public static TimeFrame valueOf(final Time start, final Time end)
    {
        return new TimeFrame(start, end);
    }

    /**
     * Creates a time frame for a start and duration
     * @param start The start time
     * @param duration The duration
     * @return The time frame
     * @throws IllegalArgumentException Thrown if start time is before end time
     */
    public static TimeFrame valueOf(final Time start, final Duration duration)
    {
        return new TimeFrame(start, start.add(duration));
    }

    /**
     * Returns a timeframe source for a start and end time-of-day. For example, called
     * with 3pm and 5pm as parameters, the timeframe source returned would produce
     * timeframe objects representing 3pm-5pm on whatever day it is when the caller calls
     * the timeframesource interface.
     * @param startTimeOfDay The start time for this time frame each day
     * @param endTimeOfDay The end time for this time frame each day
     * @return A timeframe source which will return the specified timeframe each day
     */
    public static ITimeFrameSource eachDay(final TimeOfDay startTimeOfDay,
            final TimeOfDay endTimeOfDay)
    {
        check(startTimeOfDay, endTimeOfDay);

        return new ITimeFrameSource()
        {
            public TimeFrame get()
            {
                return new TimeFrame(Time.valueOf(startTimeOfDay), Time.valueOf(endTimeOfDay));
            }
        };
    }

    /**
     * Checks consistency of start and end values
     * @param start Start value
     * @param end End value
     */
    private static void check(final AbstractTimeValue start, final AbstractTimeValue end)
    {
        // Throw illegal argument exception if start is less than end
        if (end.lessThan(start))
        {
            throw new IllegalArgumentException("Start time of time frame "
                    + start + " was after end time " + end);
        }
    }

    /**
     * @return The duration of this time frame
     */
    public Duration getDuration()
    {
        return end.subtract(start);
    }

    /**
     * @param time The time to check
     * @return True if this time frame contains the given time
     */
    public boolean contains(final Time time)
    {
        return (start.equals(time) || start.before(time)) && end.after(time);
    }

    /**
     * @param timeframe The timeframe to test
     * @return True if the given timeframe overlaps this one
     */
    public boolean overlaps(final TimeFrame timeframe)
    {
        return contains(timeframe.start)
                || contains(timeframe.end) || timeframe.contains(start) || timeframe.contains(end);
    }

    /**
     * @return The start of this time frame
     */
    public Time getStart()
    {
        return start;
    }

    /**
     * @return The end of this time frame
     */
    public Time getEnd()
    {
        return end;
    }

    /**
     * @return String representation of this object
     */
    public String toString()
    {
        return "[start=" + start + ", end=" + end + "]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
