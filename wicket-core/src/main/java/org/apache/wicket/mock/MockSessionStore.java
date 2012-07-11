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
package org.apache.wicket.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.wicket.Session;
import org.apache.wicket.request.Request;
import org.apache.wicket.session.ISessionStore;

/**
 * Session store that holds one session.
 * 
 * @author Matej Knopp
 */
public class MockSessionStore implements ISessionStore
{
	/**
	 * Construct.
	 */
	public MockSessionStore()
	{
	}

	private String sessionId;
	private final Map<String, Serializable> attributes = new HashMap<String, Serializable>();
	private final Set<UnboundListener> unboundListeners = new CopyOnWriteArraySet<UnboundListener>();
	private final Set<BindListener> bindListeners = new CopyOnWriteArraySet<BindListener>();

	private Session session;

	@Override
	public void bind(Request request, Session newSession)
	{
		session = newSession;
	}

	@Override
	public void destroy()
	{
		cleanup();
	}

	@Override
	public Serializable getAttribute(Request request, String name)
	{
		return attributes.get(name);
	}

	@Override
	public List<String> getAttributeNames(Request request)
	{
		return Collections.unmodifiableList(new ArrayList<String>(attributes.keySet()));
	}

	@Override
	public String getSessionId(Request request, boolean create)
	{
		if (create && sessionId == null)
		{
			sessionId = UUID.randomUUID().toString();
		}
		return sessionId;
	}

	private void cleanup()
	{
		sessionId = null;
		attributes.clear();
		session = null;
	}

	@Override
	public void invalidate(Request request)
	{
		String sessId = sessionId;
		cleanup();
		for (UnboundListener l : unboundListeners)
		{
			l.sessionUnbound(sessId);
		}

	}

	@Override
	public Session lookup(Request request)
	{
		return session;
	}

	@Override
	public void registerUnboundListener(UnboundListener listener)
	{
		unboundListeners.add(listener);
	}

	@Override
	public void removeAttribute(Request request, String name)
	{
		attributes.remove(name);
	}

	@Override
	public final Set<UnboundListener> getUnboundListener()
	{
		return Collections.unmodifiableSet(unboundListeners);
	}

	@Override
	public void setAttribute(Request request, String name, Serializable value)
	{
		attributes.put(name, value);
	}

	@Override
	public void unregisterUnboundListener(UnboundListener listener)
	{
		unboundListeners.remove(listener);
	}

	@Override
	public void registerBindListener(BindListener listener)
	{
		bindListeners.add(listener);
	}

	@Override
	public void unregisterBindListener(BindListener listener)
	{
		bindListeners.remove(listener);
	}

	@Override
	public Set<BindListener> getBindListeners()
	{
		return Collections.unmodifiableSet(bindListeners);
	}

	@Override
	public void flushSession(Request request, Session session)
	{
		this.session = session;
	}

}
