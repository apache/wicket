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
package org.apache.wicket.pageStore;

import java.io.Serializable;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Default page context using {@link RequestCycle#getRequest()} and {@link Session#get()}. 
 * 
 * @author Juergen Donnerstag
 * @author svenmeier
 */
public class DefaultPageContext implements IPageContext
{
	
	/**
	 * @see org.apache.wicket.pageStore.IPageContext#getSessionId()
	 */
	@Override
	public String getSessionId()
	{
		Session session = Session.get();
		
		session.bind();
		
		return session.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getSessionAttribute(String key)
	{
		return (T)Session.get().getAttribute(key);
	}
	
	@Override
	public <T extends Serializable> void setSessionAttribute(String key, T value)
	{
		Session session = Session.get();
		session.bind();
		session.setAttribute(key, value);
	}
	
	@Override
	public <T extends Serializable> T getSessionData(MetaDataKey<T> key)
	{
		return Session.get().getMetaData(key);
	}

	@Override
	public <T extends Serializable> T setSessionData(MetaDataKey<T> key, T value)
	{
		Session session = Session.get();

		synchronized (session)
		{
			T oldValue = session.getMetaData(key);
			if (oldValue != null) {
				return oldValue;
			}
			
			session.bind();
			session.setMetaData(key, value);
			return value;
		}
	}

	@Override
	public <T> T getRequestData(MetaDataKey<T> key)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException("Not a request thread.");
		}
		return requestCycle.getMetaData(key);
	}

	@Override
	public <T> void setRequestData(MetaDataKey<T> key, T value)
	{
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle == null)
		{
			throw new IllegalStateException("Not a request thread.");
		}
		requestCycle.setMetaData(key, value);
	}
}
