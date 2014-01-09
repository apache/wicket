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
package org.apache.wicket.atmosphere;

import com.google.common.base.Objects;

/**
 * Identifies a page by its id and the session it belongs to.
 * 
 * @author papegaaij
 */
public class PageKey
{
	private Integer pageId;

	private String sessionId;

	/**
	 * Construct.
	 * 
	 * @param pageId
	 * @param sessionId
	 */
	public PageKey(Integer pageId, String sessionId)
	{
		this.pageId = pageId;
		this.sessionId = sessionId;
	}

	/**
	 * @return The id of the page
	 */
	public Integer getPageId()
	{
		return pageId;
	}

	/**
	 * @return The id of the session
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * @param sessionId
	 * @return true if this {@code PageKey} is for the same session
	 */
	public boolean isForSession(String sessionId)
	{
		String sid = getSessionId();
		return sid != null && sid.equals(sessionId);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(pageId, sessionId);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PageKey)
		{
			PageKey other = (PageKey)obj;
			return Objects.equal(pageId, other.pageId) && Objects.equal(sessionId, other.sessionId);
		}
		return false;
	}
}
