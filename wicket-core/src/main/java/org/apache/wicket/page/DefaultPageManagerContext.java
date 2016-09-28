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
package org.apache.wicket.page;

import java.io.Serializable;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Wicket's default page manager context
 * 
 * @author Juergen Donnerstag
 */
public class DefaultPageManagerContext implements IPageManagerContext
{
	private static final MetaDataKey<Object> requestCycleMetaDataKey = new MetaDataKey<Object>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#bind()
	 */
	@Override
	public void bind()
	{
		Session.get().bind();
	}

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#getRequestData()
	 */
	@Override
	public Object getRequestData()
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException("Not a request thread.");
		}
		return requestCycle.getMetaData(requestCycleMetaDataKey);
	}

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#getSessionAttribute(java.lang.String)
	 */
	@Override
	public Serializable getSessionAttribute(final String key)
	{
		return Session.get().getAttribute(key);
	}

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#getSessionId()
	 */
	@Override
	public String getSessionId()
	{
		return Session.get().getId();
	}

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#setRequestData(Object)
	 */
	@Override
	public void setRequestData(final Object data)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException("Not a request thread.");
		}
		requestCycle.setMetaData(requestCycleMetaDataKey, data);
	}

	/**
	 * @see org.apache.wicket.page.IPageManagerContext#setSessionAttribute(java.lang.String,
	 *      java.io.Serializable)
	 */
	@Override
	public void setSessionAttribute(String key, Serializable value)
	{
		Session.get().setAttribute(key, value);
	}
}
