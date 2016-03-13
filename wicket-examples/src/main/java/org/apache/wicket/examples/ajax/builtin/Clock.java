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
package org.apache.wicket.examples.ajax.builtin;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;


/**
 * A simple component that displays current time
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class Clock extends Label
{
	/**
	 * Constructor
	 * 
	 * @param id
	 *            Component id
	 * @param tz
	 *            Timezone
	 */
	public Clock(String id, TimeZone tz)
	{
		super(id, new ClockModel(tz));

	}

	/**
	 * A model that returns current time in the specified timezone via a formatted string
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static class ClockModel implements IModel<String>
	{
		private final DateFormat df;

		/**
		 * @param tz
		 */
		public ClockModel(TimeZone tz)
		{
			df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG);
			df.setTimeZone(tz);
		}

		@Override
		public String getObject()
		{
			return df.format(new Date());
		}
	}
}
