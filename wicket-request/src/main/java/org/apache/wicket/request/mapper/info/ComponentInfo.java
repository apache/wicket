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
 * Encodes listener and component path in form of
 * {@code <listener>-<componentPath>},
 * {@code <listener>.<behaviorIndex>-<componentPath>} or
 * {@code <render-count>.<listener>.<behaviorIndex>-<componentPath>}
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
			int length = path.length();
			if (length == 0)
			{
				return path.toString();
			}
			StringBuilder result = new StringBuilder(length);
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
			int length = path.length();
			if (length == 0)
			{
				return path.toString();
			}
			StringBuilder result = new StringBuilder(length);
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

	private final String componentPath;
	private final Integer behaviorId;
	private final Integer renderCount;

	/**
	 * Construct.
	 * 
	 * @param renderCount
	 * @param componentPath
	 * @param behaviorId
	 */
	public ComponentInfo(final Integer renderCount, final String componentPath, final Integer behaviorId)
	{
		Args.notNull(componentPath, "componentPath");

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
		String path = encodeComponentPath(componentPath);
		StringBuilder result = new StringBuilder(path.length() + 12);

		if (renderCount != null)
		{
			result.append(renderCount);
		}

		if (renderCount != null || behaviorId != null) {
			result.append(BEHAVIOR_INDEX_SEPARATOR);
		}
		
		if (behaviorId != null)
		{
			result.append(behaviorId);
		}
		result.append(SEPARATOR);
		result.append(path);

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
		if (string == null || string.isEmpty())
		{
			return false;
		}
		for (int i = 0; i < string.length(); ++i)
		{
			if (!Character.isDigit(string.charAt(i)))
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
			String listener = string.substring(0, i);
			String componentPath = decodeComponentPath(string.substring(i + 1));

			Integer behaviorIndex = null;
			Integer renderCount = null;

			String listenerParts[] = Strings.split(listener, BEHAVIOR_INDEX_SEPARATOR);
			if (listenerParts.length == 0)
			{
				return new ComponentInfo(renderCount, componentPath, behaviorIndex);
			}
			else if (listenerParts.length == 2)
			{
				if (isNumber(listenerParts[0]))
				{
					renderCount = Integer.valueOf(listenerParts[0]);
				}
				if (isNumber(listenerParts[1]))
				{
					behaviorIndex = Integer.valueOf(listenerParts[1]);
				}
				
				return new ComponentInfo(renderCount, componentPath, behaviorIndex);
			}
			else
			{
				return null;
			}
		}
	}
}
