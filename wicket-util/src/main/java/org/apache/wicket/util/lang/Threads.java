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

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;

/**
 * A utility class for dealing with {@link Thread}s.
 */
public class Threads
{

	private Threads()
	{
	}

	/**
	 * Dumps the threads' stack traces in {@link System#out}.
	 * 
	 * @see #dumpAllThreads(PrintStream)
	 */
	public static void dumpAllThreads()
	{
		dumpAllThreads(System.out);
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
	 * @param out
	 *            the output stream where the collected information will be written
	 */
	public static void dumpAllThreads(final PrintStream out)
	{
		Args.notNull(out, "out");

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

		StringBuilder dump = new StringBuilder();

		String newLine = System.getProperty("line.separator", "\n");

		String format = "\"${name}\"${isDaemon} prio=${priority} tid=${threadIdDec} state=${state} ";

		dump.append("Full thread dump ")
			.append(runtimeMXBean.getVmName())
			.append('(')
			.append(runtimeMXBean.getVmVersion())
			.append(')');
		dump.append(newLine).append(newLine);

		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		Iterator<Entry<Thread, StackTraceElement[]>> itor = allStackTraces.entrySet().iterator();
		while (itor.hasNext())
		{
			Entry<Thread, StackTraceElement[]> entry = itor.next();
			Thread thread = entry.getKey();

			Map<CharSequence, Object> variables = new HashMap<CharSequence, Object>();
			variables.put("name", thread.getName());
			variables.put("isDaemon", thread.isDaemon() ? " daemon" : "");
			variables.put("priority", thread.getPriority());
			variables.put("threadIdDec", thread.getId());
			variables.put("state", thread.getState());

			String interpolated = MapVariableInterpolator.interpolate(format, variables);
			dump.append(interpolated).append(newLine);

			StackTraceElement[] traceElements = entry.getValue();
			for (int i = 0; i < traceElements.length; i++)
			{
				StackTraceElement element = traceElements[i];

				dump.append("\tat ")
					.append(element.getClassName())
					.append('.')
					.append(element.getMethodName())
					.append('(');

				if (element.getLineNumber() > 0)
				{
					dump.append(element.getFileName()).append(':').append(element.getLineNumber());
				}
				else
				{
					dump.append("Native method");
				}

				dump.append(')').append(newLine);
			}

			dump.append(newLine);
		}

		out.println(dump.toString());
	}

}
