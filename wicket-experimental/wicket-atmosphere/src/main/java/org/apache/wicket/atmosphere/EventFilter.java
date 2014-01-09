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

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

/**
 * Used by {@link EventBus} to filters subscriptions for a given event. Both event type and
 * {@linkplain Subscribe#filter() subscription filter} are taken into account.
 * 
 * @author papegaaij
 */
public class EventFilter implements Predicate<EventSubscription>
{
	private AtmosphereEvent event;

	/**
	 * Construct.
	 * 
	 * @param event
	 */
	public EventFilter(AtmosphereEvent event)
	{
		this.event = event;
	}

	@Override
	public boolean apply(@Nullable EventSubscription input)
	{
		return input.getFilter().apply(event);
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		return super.equals(other);
	}
}
