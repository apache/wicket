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

import org.atmosphere.cpr.AtmosphereResource;

/**
 * The event fired by {@link EventBus}, containing the payload and the {@code AtmosphereResource} it
 * is targeted at.
 * 
 * @author papegaaij
 */
public class AtmosphereEvent
{
	private final Object payload;

	private final AtmosphereResource resource;

	AtmosphereEvent(Object payload, AtmosphereResource resource)
	{
		this.payload = payload;
		this.resource = resource;
	}

	/**
	 * @return The payload of the event, as posted on the {@link EventBus}.
	 */
	public Object getPayload()
	{
		return payload;
	}

	/**
	 * @return The resource this event is targeted at.
	 */
	public AtmosphereResource getResource()
	{
		return resource;
	}
}
