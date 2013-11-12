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
 * Delivers an event to a component. Developers can implement and register their dispatchers in
 * {@link org.apache.wicket.settings.def.FrameworkSettings} to create custom strategies for
 * how events get delivered to components
 * 
 * @see IEventSink
 * @see IComponentAwareEventSink
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IEventDispatcher
{
	/**
	 * Dispatches the even to the target component
	 * 
	 * @param sink
	 *            the sink for the event. Sinks usually implement {@link IEventSink} or
	 *            {@link IComponentAwareEventSink}. See the {@code component} parameter described
	 *            below.
	 * @param event
	 * @param component
	 *            provides context to the sink. Some sinks are owned by the component, eg
	 *            {@link Behavior}s, and thus it is useful for them to have a reference to their
	 *            owning component. If this method is not {@code null} the dispatcher should try to
	 *            look for an alternative sink method which takes a component reference as an
	 *            additional parameter, one such implementation is {@link IComponentAwareEventSink}.
	 */
	void dispatchEvent(Object sink, IEvent<?> event, Component component);
}
