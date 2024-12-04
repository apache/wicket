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
package org.apache.wicket.util.lang;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;
import org.slf4j.Logger;

/**
 * A utility class for dealing with {@link Thread}s.
 */
public class Threads
{

	private static final String FORMAT = "\"${name}\"${isDaemon} prio=${priority} tid=${threadIdDec} state=${state} ";

	private Threads()
	{
	}

	/**
	 * Creates a dump of all the threads' state and stack traces similar to what JVM produces when
	 * signal SIGQUIT is send to the process on Unix machine.
	 * <p>
	 * Note: This is a best effort to dump as much information as possible because the Java API
	 * doesn't provide means to get all the information that is produced by jstack program for
	 * example.
	 * </p>
	 * 
	 * @param logger
	 *            the logger where the collected information will be written
	 */
	public static void dumpAllThreads(Logger logger)
	{
		Args.notNull(logger, "logger");
		if (!logger.isWarnEnabled())
		{
			return;
		}

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

		StringBuilder dump = new StringBuilder();

		dump.append("Full thread dump ")
			.append(runtimeMXBean.getVmName())
			.append('(')
			.append(runtimeMXBean.getVmVersion())
			.append(')');
		logger.warn(dump.toString());

		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		for (Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet())
		{
			dumpSingleThread(logger, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates a dump of the threads' state and stack traces similar to the one that the JVM
	 * produces when signal SIGQUIT is send to the process on Unix machine.
	 * <p>
	 * Note: This is a best effort to dump as much information as possible because the Java API
	 * doesn't provide means to get all the information that is produced by jstack program for
	 * example.
	 * </p>
	 * 
	 * @param logger
	 *            the logger where the collected information will be written
	 * @param thread
	 *            the thread to dump
	 */
	public static void dumpSingleThread(Logger logger, Thread thread)
	{
		Args.notNull(logger, "logger");
		if (!logger.isWarnEnabled())
		{
			return;
		}

		dumpSingleThread(logger, thread, thread.getStackTrace());
	}

	private static void dumpSingleThread(Logger logger, Thread thread, StackTraceElement[] trace)
	{
		Map<CharSequence, Object> variables = new HashMap<>();
		variables.put("name", thread.getName());
		variables.put("isDaemon", thread.isDaemon() ? " daemon" : "");
		variables.put("priority", thread.getPriority());
		variables.put("threadIdDec", thread.getId());
		variables.put("state", thread.getState());

		ThreadDump throwable = new ThreadDump();
		throwable.setStackTrace(trace);
		logger.warn(MapVariableInterpolator.interpolate(FORMAT, variables), throwable);
	}

	/**
	 * An exception used to hold the stacktrace of a thread.
	 */
	private static class ThreadDump extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see java.lang.Throwable#fillInStackTrace()
		 */
		@Override
		public synchronized Throwable fillInStackTrace()
		{
			// don't waste time to load the stack of the current thread
			return null;
		}
	}
}
