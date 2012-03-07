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
package org.apache.wicket.core.util.string;

import org.apache.wicket.Component;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 *
 */
public class ComponentStrings
{
	/**
	 * Construct.
	 */
	private ComponentStrings()
	{
	}

	/**
	 * Creates a location stacktrace string representation for the component for reference when the
	 * render check fails. This method filters out most of the unnecessary parts of the stack trace.
	 * The message of the <code>location</code> is used as a verb in the rendered string. Use
	 * "added", "constructed" or similar verbs as values.
	 *
	 * @param component
	 *            the component that was constructed or added and failed to render
	 * @param location
	 *            the location where the component was created or added in the java code.
	 * @return a string giving the line precise location where the component was added or created.
	 */
	public static String toString(final Component component, final Throwable location)
	{
		Class<?> componentClass = component.getClass();

		// try to find the component type, if it is an inner element, then get
		// the parent component.
		String componentType = componentClass.getName();
		if (componentType.indexOf('$') >= 0)
		{
			componentType = componentClass.getSuperclass().getName();
		}

		componentType = componentType.substring(componentType.lastIndexOf('.') + 1);

		// create a user friendly message, using the location's message as a
		// differentiator for the message (e.g. "component foo was ***added***"
		// or "component foo was ***created***")
		AppendingStringBuffer sb = new AppendingStringBuffer("The " + componentType.toLowerCase() +
			" with id '" + component.getId() + "' that failed to render was " +
			location.getMessage() + "\n");

		// a list of stacktrace elements that need to be skipped in the location
		// stack trace
		String[] skippedElements = new String[] { "org.apache.wicket.MarkupContainer",
				"org.apache.wicket.Component", "org.apache.wicket.markup" };

		// a list of stack trace elements that stop the traversal of the stack
		// trace
		String[] breakingElements = new String[] { "org.apache.wicket.protocol.http.WicketServlet",
				"org.apache.wicket.protocol.http.WicketFilter", "java.lang.reflect" };

		StackTraceElement[] trace = location.getStackTrace();
		for (int i = 0; i < trace.length; i++)
		{
			String traceString = trace[i].toString();
			if (shouldSkip(traceString, skippedElements))
			{
				// don't print this line, is wicket internal
				continue;
			}

			if (!(traceString.startsWith("sun.reflect.") && i > 1))
			{
				// filter out reflection API calls from the stack trace
				if (!traceString.contains("java.lang.reflect"))
				{
					sb.append("     at ");
					sb.append(traceString);
					sb.append("\n");
				}
				if (shouldSkip(traceString, breakingElements))
				{
					break;
				}
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	private static boolean shouldSkip(String text, String[] filters)
	{
		for (String filter : filters)
		{
			if (text.contains(filter))
			{
				return true;
			}
		}
		return false;
	}
}
