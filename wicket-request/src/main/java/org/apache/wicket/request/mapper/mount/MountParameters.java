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
package org.apache.wicket.request.mapper.mount;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.util.string.StringValue;

/**
 * A container for the placeholders (e.g. ${placeholder}) found in the mount segments
 * 
 * @author igor.vaynberg
 * @deprecated Will be removed in Wicket 8.0
 */
@Deprecated
public class MountParameters
{
	private final Map<String, String> map = new HashMap<>();

	/**
	 * 
	 * @param parameterName
	 *            the name of the placeholder
	 * @return a StringValue which contains either the actual value if there is a placeholder with
	 *         name <code>parameterName</code> or <code>null</code> otherwise
	 */
	public final StringValue getValue(final String parameterName)
	{
		return StringValue.valueOf(map.get(parameterName));
	}

	/**
	 * Sets new placeholder name/pair
	 * 
	 * @param parameterName
	 * @param value
	 */
	public final void setValue(final String parameterName, final StringValue value)
	{
		map.put(parameterName, value.toString());
	}

	/**
	 * @return an unmodifiable view of the parameters names
	 */
	public final Collection<String> getParameterNames()
	{
		return Collections.unmodifiableCollection(map.keySet());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		MountParameters other = (MountParameters)obj;
		if (map == null)
		{
			if (other.map != null)
			{
				return false;
			}
		}
		else if (!map.equals(other.map))
		{
			return false;
		}
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MountParameters [" + map + "]";
	}
}
