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


import org.apache.wicket.util.io.IClusterable;

/**
 * Unique identifier of a page instance
 * 
 * @author igor.vaynberg
 */
public class PageReference implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private final int pageId;

	/**
	 * Constructor
	 * 
	 * @param pageId
	 */
	public PageReference(int pageId)
	{
		this.pageId = pageId;
	}

	/**
	 * @return The page that the this PageReference references
	 */
	public Page getPage()
	{
		return (Page)Session.get().getPageManager().getPage(pageId);
	}

	/**
	 * Gets pageId.
	 * 
	 * @return pageId
	 */
	public int getPageId()
	{
		return pageId;
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return pageId;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
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
		PageReference other = (PageReference)obj;
		return getPageId() == other.getPageId();
	}


}
