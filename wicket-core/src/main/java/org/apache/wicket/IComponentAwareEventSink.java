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

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;

/**
 * A specialization of {@link IEventSink} that adds component as an additional parameter to the
 * {@link #onEvent(Component, IEvent)} method. This interface is useful for component plugins which
 * wish to participate in event processing, for example {@link Behavior}s
 * 
 * @author igor
 */
public interface IComponentAwareEventSink
{
	/**
	 * Called when an event is sent to this behavior sink
	 * 
	 * @param component
	 *            component that owns this sink. For example, if the implementation of this
	 *            interface is a {@link Behavior} then component parameter will contain the
	 *            component to which the behavior is attached.
	 * @param event
	 */
	void onEvent(Component component, IEvent<?> event);
}
