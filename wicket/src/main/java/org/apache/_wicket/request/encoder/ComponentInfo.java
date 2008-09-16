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
package org.apache._wicket.request.encoder;

import org.apache.wicket.util.string.Strings;

/**
 * Encodes listener inteface and component path in form of
 * &lt;listenerInterface&gt-&lt;componentPath&gt; Also component path is escaped (':' characters are
 * replaced by '-')
 * 
 * @author Matej Knopp
 */
class ComponentInfo
{
	private final String listenerInterface;
	private final String componentPath;

	private static final char SEPARATOR = '-';

	public ComponentInfo(String listenerInterface, String componentPath)
	{
		this.listenerInterface = listenerInterface;
		this.componentPath = componentPath;
	}

	public String getComponentPath()
	{
		return componentPath;
	}

	public String getListenerInterface()
	{
		return listenerInterface;
	}

	private static final String TMP_PLACEHOLDER = "[[[[[[[WICKET[[TMP]]DASH]]" + Math.random() +
		"]]]]";

	private static String encodeComponentPath(String path)
	{
		if (path != null)
		{
			path = path.replace("" + SEPARATOR, TMP_PLACEHOLDER);
			path = path.replace(':', SEPARATOR);
			path = path.replace(TMP_PLACEHOLDER, "" + SEPARATOR + SEPARATOR);
			return path;
		}
		else
		{
			return null;
		}
	}

	private static String decodeComponentPath(String path)
	{
		if (path != null)
		{
			path = path.replace("" + SEPARATOR + SEPARATOR, TMP_PLACEHOLDER);
			path = path.replace(SEPARATOR, ':');
			path = path.replace(TMP_PLACEHOLDER, "" + SEPARATOR);
			return path;
		}
		else
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		if (listenerInterface != null)
		{
			result.append(listenerInterface);
		}
		result.append(SEPARATOR);
		if (componentPath != null)
		{
			result.append(encodeComponentPath(componentPath));
		}
		return result.toString();
	}

	/**
	 * Parses the given string.
	 * 
	 * @param string
	 * @return component info or <code>null</code> if the string is not in correct format.
	 */
	public static ComponentInfo parse(String string)
	{
		if (Strings.isEmpty(string))
		{
			return null;
		}
		int i = string.indexOf(SEPARATOR);
		if (i == -1)
		{
			return null;
		}
		else
		{
			String listenerInterface = string.substring(0, i);
			String componentPath = decodeComponentPath(string.substring(i + 1));

			if (Strings.isEmpty(listenerInterface))
			{
				listenerInterface = null;
			}
			if (Strings.isEmpty(componentPath))
			{
				componentPath = null;
			}
			return new ComponentInfo(listenerInterface, componentPath);
		}
	}

}
