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
package org.apache.wicket.protocol.http;

import java.util.Map;

import org.apache.wicket.util.collections.MostRecentlyUsedMap;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 * A map that contains the buffered responses. It has a constraint on the maximum entries that it
 * can contain, and a constraint on the duration of time an entry is considered valid/non-expired
 */
class StoredResponsesMap extends MostRecentlyUsedMap<String, Object>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The actual object that is stored as a value of the map. It wraps the buffered response and
	 * assigns it a creation time.
	 */
	private static class Value
	{
		/** the original response to store */
		private BufferedWebResponse response;

		/** the time when this response is stored */
		private Time creationTime;
	}

	/**
	 * The duration of time before a {@link Value} is considered as expired
	 */
	private final Duration lifetime;

	/**
	 * Construct.
	 * 
	 * @param maxEntries
	 *            how much entries this map can contain
	 * @param lifetime
	 *            the duration of time to keep an entry in the map before considering it expired
	 */
	public StoredResponsesMap(int maxEntries, Duration lifetime)
	{
		super(maxEntries);

		this.lifetime = lifetime;
	}

	@Override
	protected synchronized boolean removeEldestEntry(java.util.Map.Entry<String, Object> eldest)
	{
		boolean removed = super.removeEldestEntry(eldest);
		if (removed == false)
		{
			Value value = (Value)eldest.getValue();
			if (value != null)
			{
				Duration elapsedTime = Time.now().subtract(value.creationTime);
				if (lifetime.lessThanOrEqual(elapsedTime))
				{
					removedValue = value.response;
					removed = true;
				}
			}
		}
		return removed;
	}

	@Override
	public BufferedWebResponse put(String key, Object bufferedResponse)
	{
		if (!(bufferedResponse instanceof BufferedWebResponse))
		{
			throw new IllegalArgumentException(StoredResponsesMap.class.getSimpleName() +
				" can store only instances of " + BufferedWebResponse.class.getSimpleName());
		}

		Value value = new Value();
		value.creationTime = Time.now();
		value.response = (BufferedWebResponse)bufferedResponse;

		Value oldValue;
		synchronized (this)
		{
			oldValue = (Value)super.put(key, value);
		}

		return oldValue != null ? oldValue.response : null;
	}

	@Override
	public BufferedWebResponse get(Object key)
	{
		BufferedWebResponse result = null;
		Value value;
		synchronized (this)
		{
			value = (Value)super.get(key);
		}
		if (value != null)
		{
			Duration elapsedTime = Time.now().subtract(value.creationTime);
			if (lifetime.greaterThan(elapsedTime))
			{
				result = value.response;
			}
			else
			{
				// expired, remove it
				remove(key);
			}
		}
		return result;
	}

	@Override
	public BufferedWebResponse remove(Object key)
	{
		Value removedValue;
		synchronized (this)
		{
			removedValue = (Value)super.remove(key);
		}

		return removedValue != null ? removedValue.response : null;
	}

	@Override
	public void putAll(Map<? extends String, ?> m)
	{
		throw new UnsupportedOperationException();
	}
}
