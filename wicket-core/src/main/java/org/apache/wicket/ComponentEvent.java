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

import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.event.IEventSource;

/**
 * Implementation of {@link IEvent} raised by a component
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of payload
 */
final class ComponentEvent<T> implements IEvent<T>
{
	private final IEventSink sink;
	private final IEventSource source;
	private final Broadcast type;
	private final T payload;

	private boolean stop;
	private boolean shallow;

	/**
	 * Constructor
	 * 
	 * @param sink
	 *            sink
	 * @param source
	 *            source
	 * @param broadcast
	 *            broadcast
	 * @param payload
	 *            payload
	 */
	public ComponentEvent(IEventSink sink, IEventSource source, Broadcast broadcast, T payload)
	{
		this.sink = sink;
		this.source = source;
		type = broadcast;
		this.payload = payload;
	}

	/**
	 * @return event sink
	 */
	public IEventSink getSink()
	{
		return sink;
	}

	/** {@inheritDoc} */
	@Override
	public IEventSource getSource()
	{
		return source;
	}

	/** {@inheritDoc} */
	@Override
	public Broadcast getType()
	{
		return type;
	}

	/** {@inheritDoc} */
	@Override
	public T getPayload()
	{
		return payload;
	}

	/** {@inheritDoc} */
	@Override
	public void dontBroadcastDeeper()
	{
		shallow = true;
	}

	/** {@inheritDoc} */
	@Override
	public void stop()
	{
		stop = true;
	}

	boolean isStop()
	{
		return stop;
	}

	boolean isShallow()
	{
		return shallow;
	}

	void resetShallow()
	{
		shallow = false;
	}
}