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

import org.atmosphere.cpr.AtmosphereResourceEvent;

/**
 * An event that is broadcasted by the EventBus whenever Atmosphere notifies
 * {@linkplain org.apache.wicket.atmosphere.AtmosphereBehavior} about {@link org.atmosphere.cpr.AtmosphereResourceEventListener}
 * events.
 *
 * To be notified add a method like the one below to your components:
 * <pre><code>{@literal
 *      @Subscribe
 *      public void anyName(AjaxRequestTarget target, AtmosphereInternalEvent event) {
 *          switch (event.getType()) {
 *              case Resume: ...; break;
 *              case Disconnect: ...; break;
 *              ....
 *          }
 *      }
 * }</code></pre>
 *
 * @see org.apache.wicket.atmosphere.EventBus#wantAtmosphereNotifications
 * @see org.atmosphere.cpr.AtmosphereResourceEventListener
 */
public class AtmosphereInternalEvent
{
	/**
	 * The types enumerating the notification methods in {@link org.atmosphere.cpr.AtmosphereResourceEventListener}
	 *
	 * Suspend type is not supported because it is not possible to push messages with suspended connection
	 */
	public static enum Type
	{
		PreSuspend, /*Suspend,*/ Resume, Disconnect, Broadcast, Throwable, Close
	}

	/**
	 * The internal Atmosphere event
	 */
	private final AtmosphereResourceEvent event;

	/**
	 * The type of the notification
	 */
	private final Type type;


	public AtmosphereInternalEvent(Type type, AtmosphereResourceEvent event)
	{
		this.type = type;
		this.event = event;
	}

	public AtmosphereResourceEvent getEvent()
	{
		return event;
	}

	public Type getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return "AtmosphereInternalEvent{" +
				"event=" + event +
				", type=" + type +
				'}';
	}
}
