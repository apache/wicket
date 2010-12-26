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
package org.apache.wicket.event;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Defines the event broadcast type.
 * 
 * @author igor
 */
public enum Broadcast {
	/**
	 * Breadth first traversal. Supported sinks in order of traversal:
	 * 
	 * <ol>
	 * <li>{@link Application}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Page}</li>
	 * <li>{@link Component}s</li>
	 * </ol>
	 * 
	 * Any sink along the path can be specified and traversal will start with the specified sink as
	 * root, eg:
	 * 
	 * <ul>
	 * <li>If a component inside the page is specified then only the component and all its children
	 * will receive the event</li>
	 * <li>If Session is specified then the session, the request cycle, the page and all its
	 * components will receive the event</li>
	 * </ul>
	 */
	BREADTH,
	/**
	 * Depth first traversal. Supported sinks in order of traversal:
	 * 
	 * <ol>
	 * <li>{@link Component}s</li>
	 * <li>{@link Page}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link Application}</li>
	 * </ol>
	 * 
	 * Any sink along the path can be specified and traversal will start with the specified sink as
	 * root, eg:
	 * 
	 * <ul>
	 * <li>If a component inside the page is specified then only the component and all its children
	 * will receive the event</li>
	 * <li>If Session is specified then the session, the request cycle, the page and all its
	 * components will receive the event</li>
	 * </ul>
	 * 
	 */
	DEPTH,
	/**
	 * A bubble-up traversal. In a bubble-up traversal only the sink and its parents are notified.
	 * 
	 * Supported sinks in order of traversal are:
	 * <ol>
	 * <li>{@link Component}s</li>
	 * <li>{@link Page}</li>
	 * <li>{@link RequestCycle}</li>
	 * <li>{@link Session}</li>
	 * <li>{@link Application}</li>
	 * </ol>
	 * 
	 * Any sink along the path can be specified and traversal will start at the specified sink and
	 * work its way up to the {@link Application}, eg:
	 * 
	 * <ul>
	 * <li>If a component inside the page is specified then only the component, its parents, the
	 * request cycle, the session, and the application will be notified.
	 * <li>If Session is specified then the session, the application will be notified</li>
	 * </ul>
	 */
	BUBBLE,
	/**
	 * Only the specified sink receives the event
	 */
	EXACT
}
