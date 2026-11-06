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
package org.apache.wicket.islands;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.util.lang.Args;

/**
 * Keeps handles in the session, the most recently used ones up to a capacity. A handle is a random
 * token from the application's secure random supplier; it dies with the session and does not
 * resolve in another one. Issuing a handle binds a temporary session.
 * <p>
 * The contexts are stored in the session, so they must be small; the session is replicated with
 * them where the application replicates sessions. A handle that has been pushed out by newer ones
 * no longer resolves, and the call is answered as expired.
 *
 * @since 12.0.0
 */
public class SessionHandleStore implements IIslandHandleStore
{
	/**
	 * The number of handles a session keeps by default.
	 */
	public static final int DEFAULT_CAPACITY = 1000;

	private static final int HANDLE_BYTES = 24;

	private static final MetaDataKey<Handles> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private record Issued(String endpointId, Serializable context) implements Serializable
	{
	}

	private static final class Handles implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Map<String, Issued> entries;

		private Handles(int capacity)
		{
			entries = new LinkedHashMap<>(16, 0.75f, true)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<String, Issued> eldest)
				{
					return size() > capacity;
				}
			};
		}

		private synchronized void put(String handle, Issued issued)
		{
			entries.put(handle, issued);
		}

		private synchronized Issued get(String handle)
		{
			return entries.get(handle);
		}
	}

	private final int capacity;

	/**
	 * Keeps {@value #DEFAULT_CAPACITY} handles per session.
	 */
	public SessionHandleStore()
	{
		this(DEFAULT_CAPACITY);
	}

	/**
	 * @param capacity
	 *            the number of handles a session keeps
	 */
	public SessionHandleStore(int capacity)
	{
		this.capacity = Args.withinRange(1, Integer.MAX_VALUE, capacity, "capacity");
	}

	@Override
	public String issue(String endpointId, Serializable context)
	{
		Args.notNull(endpointId, "endpointId");
		Args.notNull(context, "context");

		Session session = Session.get();
		if (session.isTemporary())
		{
			session.bind();
		}
		String handle = Application.get()
			.getSecuritySettings()
			.getRandomSupplier()
			.getRandomBase64(HANDLE_BYTES);
		Handles handles = handles(session, true);
		handles.put(handle, new Issued(endpointId, context));
		session.setMetaData(KEY, handles);
		return handle;
	}

	@Override
	public Serializable resolve(String endpointId, String handle)
	{
		if (handle == null)
		{
			return null;
		}
		Handles handles = handles(Session.get(), false);
		Issued issued = handles == null ? null : handles.get(handle);
		return issued != null && issued.endpointId().equals(endpointId) ? issued.context() : null;
	}

	private Handles handles(Session session, boolean create)
	{
		synchronized (session)
		{
			Handles handles = session.getMetaData(KEY);
			if (handles == null && create)
			{
				handles = new Handles(capacity);
				session.setMetaData(KEY, handles);
			}
			return handles;
		}
	}
}
