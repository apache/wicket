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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Abstract base class for subclasses that represent a point in time (as opposed
 * to a duration of time).
 * 
 * @author Jonathan Locke
 */
abstract class AbstractTime extends AbstractTimeValue
{
	/** Calendar for the local timezone */
	static final Calendar localtime = Calendar.getInstance();

	/** Time format */
	static final SimpleDateFormat timeFormat = new SimpleDateFormat("h.mma");

	/**
	 * Package local constructor for package subclasses only
	 * 
	 * @param milliseconds
	 *            The number of milliseconds in this time value
	 */
	AbstractTime(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * @param that
	 *            The time to compare with
	 * @return True if this time value is after that time value
	 */
	public final boolean after(final AbstractTimeValue that)
	{
		return greaterThan(that);
	}

	/**
	 * @param that
	 *            The time to compare with
	 * @return True if this time value is before that time value
	 */
	public final boolean before(final AbstractTimeValue that)
	{
		return lessThan(that);
	}

	/**
	 * Converts this time to a time string using the formatter h.mma
	 * 
	 * @return The date string
	 */
	public final String toTimeString()
	{
		return toTimeString(localtime);
	}

	/**
	 * Converts this time to a date string using the date formatter h.mma
	 * 
	 * @param calendar
	 *            The calendar to use in the conversion
	 * @return The date string
	 */
	public final String toTimeString(final Calendar calendar)
	{
		synchronized (timeFormat)
		{
			synchronized (calendar)
			{
				timeFormat.setCalendar(calendar);
				return timeFormat.format(new Date(getMilliseconds())).toLowerCase();
			}
		}
	}

	/**
	 * Converts this time to a string suitable for use in a filesystem name
	 * 
	 * @return This time as a formatted string
	 */
	public String toString()
	{
		return toTimeString();
	}
}
