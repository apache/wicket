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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Abstract base class for subclasses that represent a point in time (as opposed to a
 * {@link Duration} of time).
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
abstract class AbstractTime extends AbstractTimeValue
{
	private static final long serialVersionUID = 1L;

	/** calendar for the local time zone */
	static final Calendar localtime = Calendar.getInstance();

	/** time format */
	static final SimpleDateFormat timeFormat = new SimpleDateFormat("h.mma", Locale.ENGLISH);

	/**
	 * @param milliseconds
	 * @see AbstractTimeValue
	 */
	AbstractTime(final long milliseconds)
	{
		super(milliseconds);
	}

	/**
	 * Returns <code>true</code> if this <code>Time</code> value is after the given
	 * <code>Time</code> argument's value.
	 * 
	 * @param that
	 *            the <code>AbstractTimeValue</code> to compare with
	 * @return <code>true</code> if this <code>Time</code> value is after <code>that</code>
	 *         <code>Time</code> value
	 */
	public final boolean after(final AbstractTimeValue that)
	{
		return greaterThan(that);
	}

	/**
	 * Returns <code>true</code> if this <code>Time</code> value is before the given
	 * <code>Time</code> argument's value.
	 * 
	 * @param that
	 *            the <code>AbstractTimeValue</code> to compare with
	 * @return <code>true</code> if this <code>Time</code> value is before <code>that</code>
	 *         <code>Time</code> value
	 */
	public final boolean before(final AbstractTimeValue that)
	{
		return lessThan(that);
	}

	/**
	 * Converts this <code>Time</code> to a time <code>String</code> using the formatter 'h.mma'.
	 * 
	 * @return the <code>Time</code> <code>String</code>
	 */
	public final String toTimeString()
	{
		return toTimeString(localtime);
	}

	/**
	 * Converts this <code>Time</code> to a <code>Date String</code> using the <code>Date</code>
	 * formatter 'h.mma'.
	 * 
	 * @param calendar
	 *            the <code>Calendar</code> to use in the conversion
	 * @return the <code>Date</code> <code>String</code>
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
	 * Converts this <code>Time</code> to a <code>String</code> suitable for use in a file system
	 * name.
	 * 
	 * @return this <code>Time</code> as a formatted <code>String</code>
	 */
	@Override
	public String toString()
	{
		return toTimeString();
	}
}
