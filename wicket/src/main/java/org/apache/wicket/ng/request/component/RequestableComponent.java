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
package org.apache.wicket.ng.request.component;

import java.util.List;

import org.apache.wicket.ng.behavior.IBehavior;
import org.apache.wicket.ng.model.IDetachable;

/**
 * Base interface for components. The purpose of this interface is to make certain parts of Wicket
 * easier to mock and unit test.
 * 
 * @author Matej Knopp
 */
public interface RequestableComponent extends IDetachable
{
	/**
	 * Gets this component's path.
	 * 
	 * @return Colon separated path to this component in the component hierarchy
	 */
	public String getPath();

	/**
	 * Gets the id of this component.
	 * 
	 * @return The id of this component
	 */
	public String getId();
	
	/**
	 * Returns page this component belongs to.
	 * 
	 * @return page instance or <code>null</code>
	 */
	public RequestablePage getPage();
	
	/**
	 * Gets the component at the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	public RequestableComponent get(String path);
	
	/**
	 * Returns true if the listener interface method can be called on this component. Normally
	 * this would check if component is enabled and visible in hierarchy.
	 * 
	 * @return
	 */
	public boolean canCallListenerInterface();
	
	/**
	 * Gets the currently coupled {@link IBehavior}s as a unmodifiable list. Returns an empty list
	 * rather than null if there are no behaviors coupled to this component.
	 * 
	 * @return The currently coupled behaviors as a unmodifiable list
	 */
	public List<IBehavior> getBehaviors();
}
