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
package wicket.extensions.markup.html.repeater.util;

import java.io.Serializable;

/**
 * Represents sorting information of a property
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class SortParam implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String property;
	private boolean asc;

	/**
	 * @param property
	 *            sort property
	 * @param asc
	 *            sort direction
	 */
	public SortParam(String property, boolean asc)
	{
		this.property = property;
		this.asc = asc;
	}

	/**
	 * @return true if sort dir is ascending, false otherwise
	 */
	public boolean isAscending()
	{
		return asc;
	}

	/**
	 * @return sort property
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object rhs)
	{
		if (rhs instanceof SortParam)
		{
			SortParam param = (SortParam)rhs;
			return getProperty().equals(param.getProperty())
					&& isAscending() == param.isAscending();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return new StringBuffer().append("[SortParam property=").append(getProperty()).append(
				" ascending=").append(asc).append("]").toString();
	}
}
