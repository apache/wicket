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
package org.apache.wicket.ng;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ng.request.Request;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.session.SessionStore;

public class Session implements Serializable
{
	private static final long serialVersionUID = 1L;

	public Session(RequestCycle requestCycle)
	{

	}

	protected SessionStore getSessionStore()
	{
		return Application.get().getSessionStore();
	}

	private Request getRequest()
	{
		RequestCycle cycle = RequestCycle.get();
		return cycle != null ? cycle.getRequest() : null;
	}

	public void invalidateNow()
	{
		getSessionStore().invalidate(getRequest());
	}

	public void invalidate()
	{
		if (!invalidated)
		{
			RequestCycle.get().register(new RequestCycle.DetachCallback()
			{
				public void onDetach(RequestCycle requestCycle)
				{
					getSessionStore().invalidate(getRequest());
				}
			});
		}
		invalidated = true;
	}

	private boolean invalidated = false;

	public static Session get()
	{
		Session session = ThreadContext.getSession();
		if (session != null)
		{
			return session;
		}
		else
		{
			return Application.get().fetchCreateAndSetSession(RequestCycle.get());
		}
	}

	public boolean isTemporary()
	{
		return getSessionStore().getSessionId(getRequest(), false) == null;
	}

	public String getId()
	{
		return getSessionStore().getSessionId(getRequest(), false);
	}


	public void bind()
	{
		if (isTemporary())
		{
			getSessionStore().getSessionId(getRequest(), true);
			getSessionStore().bind(getRequest(), this);

			if (temporarySessionAttributes != null)
			{
				for (Map.Entry<String, Serializable> entry : temporarySessionAttributes.entrySet())
				{
					getSessionStore().setAttribute(getRequest(), entry.getKey(), entry.getValue());
				}
				temporarySessionAttributes = null;
			}
		}
	}

	protected void setAttribute(String name, Serializable value)
	{
		if (isTemporary())
		{
			if (temporarySessionAttributes == null)
			{
				temporarySessionAttributes = new HashMap<String, Serializable>();
				temporarySessionAttributes.put(name, value);
			}
		}
		else
		{
			getSessionStore().setAttribute(getRequest(), name, value);
		}
	}

	protected Serializable getAttribute(String name)
	{
		if (isTemporary())
		{
			if (temporarySessionAttributes != null)
			{
				return temporarySessionAttributes.get(name);
			}
			else
			{
				return null;
			}
		}
		else
		{
			return getSessionStore().getAttribute(getRequest(), name);
		}
	}

	protected Set<String> getAttributeNames()
	{
		if (isTemporary())
		{
			if (temporarySessionAttributes != null)
			{
				return Collections.unmodifiableSet(temporarySessionAttributes.keySet());
			}
			else
			{
				return Collections.emptySet();
			}
		}
		else
		{
			return Collections.unmodifiableSet(getSessionStore().getAttributeNames(getRequest()));
		}
	}

	/**
	 * Holds attributes for sessions that are still temporary/ not bound to a session store. Only
	 * used when {@link #isTemporary()} is true.
	 * <p>
	 * Note: this doesn't have to be synchronized, as the only time when this map is used is when a
	 * session is temporary, in which case it won't be shared between requests (it's a per request
	 * instance).
	 * </p>
	 */
	private transient Map<String, Serializable> temporarySessionAttributes;
}
