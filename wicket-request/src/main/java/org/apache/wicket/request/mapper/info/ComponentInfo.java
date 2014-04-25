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
package org.apache.wicket.request.mapper.info;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Encodes listener interface and component path in form of
 * &lt;listenerInterface&gt-&lt;componentPath&gt;,
 * &lt;listenerInterface&gt.&lt;behaviorIndex&gt;-&lt;componentPath&gt; or
 * &lt;render-count&gt;.&lt;listenerInterface&gt.&lt;behaviorIndex&gt;-&lt;componentPath&gt;
 * <p>
 * Component path is escaped (':' characters are replaced by '~')
 * 
 * @author Matej Knopp
 */
public class ComponentInfo
{
	private static final char BEHAVIOR_INDEX_SEPARATOR = '.';
	private static final char SEPARATOR = '-';
	private static final char COMPONENT_SEPARATOR = ':';
	private static final char SEPARATOR_ENCODED = '~';

	/**
	 * Replaces ':' with '-', and '-' with '~'.
	 * 
	 * @param path
	 *            the path to the component in its page
	 * @return the encoded path
	 */
	private static String encodeComponentPath(CharSequence path)
	{
		if (path != null)
		{
			StringBuilder result = new StringBuilder();
			int length = path.length();
			for (int i = 0; i < length; i++)
			{
				char c = path.charAt(i);
				switch (c)
				{
					case COMPONENT_SEPARATOR :
						result.append(SEPARATOR);
						break;
					case SEPARATOR :
						result.append(SEPARATOR_ENCODED);
						break;
					default :
						result.append(c);
				}
			}
			return result.toString();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Replaces '~' with '-' and '-' with ':'
	 * 
	 * @param path
	 *            the encoded path of the component in its page
	 * @return the (non-encoded) path of the component in its page
	 */
	private static String decodeComponentPath(CharSequence path)
	{
		if (path != null)
		{
			StringBuilder result = new StringBuilder();
			int length = path.length();
			for (int i = 0; i < length; i++)
			{
				char c = path.charAt(i);
				switch (c)
				{
					case SEPARATOR_ENCODED :
						result.append(SEPARATOR);
						break;
					case SEPARATOR :
						result.append(COMPONENT_SEPARATOR);
						break;
					default :
						result.append(c);
				}
			}
			return result.toString();
		}
		else
		{
			return null;
		}
	}

	private final String listenerInterface;
	private final String componentPath;
	private final Integer behaviorId;
	private final Integer renderCount;

	/**
	 * Construct.
	 * 
	 * @param renderCount
	 * @param listenerInterface
	 * @param componentPath
	 * @param behaviorId
	 */
	public ComponentInfo(final Integer renderCount, final String listenerInterface,
		final String componentPath, final Integer behaviorId)
	{
		Args.notEmpty(listenerInterface, "listenerInterface");
		Args.notNull(componentPath, "componentPath");

		this.listenerInterface = listenerInterface;
		this.componentPath = componentPath;
		this.behaviorId = behaviorId;
		this.renderCount = renderCount;
	}

	/**
	 * @return component path
	 */
	public String getComponentPath()
	{
		return componentPath;
	}

	/**
	 * @return listener interface name
	 */
	public String getListenerInterface()
	{
		return listenerInterface;
	}

	/**
	 * @return behavior index
	 */
	public Integer getBehaviorId()
	{
		return behaviorId;
	}

	/**
	 * 
	 * @return render count
	 */
	public Integer getRenderCount()
	{
		return renderCount;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();

		if (renderCount != null)
		{
			result.append(renderCount);
			result.append(BEHAVIOR_INDEX_SEPARATOR);
		}

		result.append(listenerInterface);

		if (behaviorId != null)
		{
			result.append(BEHAVIOR_INDEX_SEPARATOR);
			result.append(behaviorId);
		}
		result.append(SEPARATOR);
		result.append(encodeComponentPath(componentPath));

		return result.toString();
	}

	/**
	 * Method that rigidly checks if the string consists of digits only.
	 * 
	 * @param string
	 * @return whether the string consists of digits only
	 */
	private static boolean isNumber(final String string)
	{
		if ((string == null) || (string.length() == 0))
		{
			return false;
		}
		for (int i = 0; i < string.length(); ++i)
		{
			if (Character.isDigit(string.charAt(i)) == false)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Parses the given string.
	 * 
	 * @param string
	 * @return component info or <code>null</code> if the string is not in correct format.
	 */
	public static ComponentInfo parse(final String string)
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
				return null;
			}

			Integer behaviorIndex = null;
			Integer renderCount = null;

			String listenerParts[] = Strings.split(listenerInterface, BEHAVIOR_INDEX_SEPARATOR);
			if (listenerParts.length == 2)
			{
				if (isNumber(listenerParts[0]))
				{
					renderCount = Integer.valueOf(listenerParts[0]);
					listenerInterface = listenerParts[1];
				}
				else if (isNumber(listenerParts[1]))
				{
					listenerInterface = listenerParts[0];
					behaviorIndex = Integer.valueOf(listenerParts[1]);
				}
				else
				{
					return null;
				}
			}
			else if (listenerParts.length == 3)
			{
				if (!isNumber(listenerParts[0]) && !isNumber(listenerParts[1]))
				{
					return null;
				}
				renderCount = Integer.valueOf(listenerParts[0]);
				listenerInterface = listenerParts[1];
				behaviorIndex = Integer.valueOf(listenerParts[2]);
			}
			else if (listenerParts.length != 1)
			{
				return null;
			}

			return new ComponentInfo(renderCount, listenerInterface, componentPath, behaviorIndex);
		}
	}
}
