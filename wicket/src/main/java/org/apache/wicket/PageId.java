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
package org.apache.wicket;


/**
 * Unique identifier of a page instance
 * 
 * @author igor.vaynberg
 */
public final class PageId implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final String pageMapName;
	private final int number;
	private final int version;

	/**
	 * Constructor
	 * 
	 * @param pageMapName
	 * @param number
	 * @param version
	 */
	PageId(String pageMapName, int number, int version)
	{
		this.pageMapName = pageMapName;
		this.number = number;
		this.version = version;
	}

	/**
	 * Gets pageMapName.
	 * 
	 * @return pageMapName
	 */
	public String getPageMapName()
	{
		return pageMapName;
	}


	/**
	 * Gets pageId.
	 * 
	 * @return pageId
	 */
	public int getPageNumber()
	{
		return number;
	}


	/**
	 * Gets pageVersion.
	 * 
	 * @return pageVersion
	 */
	public int getPageVersion()
	{
		return version;
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + ((pageMapName == null) ? 0 : pageMapName.hashCode());
		result = prime * result + version;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageId other = (PageId)obj;
		if (number != other.number)
			return false;
		if (pageMapName == null)
		{
			if (other.pageMapName != null)
				return false;
		}
		else if (!pageMapName.equals(other.pageMapName))
			return false;
		if (version != other.version)
			return false;
		return true;
	}


}
