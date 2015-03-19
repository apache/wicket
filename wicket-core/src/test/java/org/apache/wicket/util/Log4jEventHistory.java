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
package org.apache.wicket.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log the log4j messages for further assertions
 * */
public class Log4jEventHistory extends AppenderSkeleton
{
	private List<LoggingEvent> history = new ArrayList<LoggingEvent>();

	/**
	 * @return log history
	 */
	public List<LoggingEvent> getHistory()
	{
		return history;
	}

	@Override
	public void close()
	{
	}

	@Override
	public boolean requiresLayout()
	{
		return false;
	}

	@Override
	protected void append(LoggingEvent event)
	{
		history.add(event);
	}

	/**
	 * @param level
	 * @param msg
	 * @return if this message was logged
	 */
	public boolean contains(Level level, String msg)
	{
		for (LoggingEvent event : history)
		{
			if (msg.equals(event.getMessage()) && level.equals(event.getLevel()))
			{
				return true;
			}
		}
		return false;
	}
}