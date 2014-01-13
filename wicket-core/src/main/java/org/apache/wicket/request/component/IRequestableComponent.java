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
package org.apache.wicket.request.component;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.InvalidBehaviorIdException;
import org.apache.wicket.model.IDetachable;

/**
 * Base interface for components. The purpose of this interface is to make certain parts of Wicket
 * easier to mock and unit test.
 * 
 * @author Matej Knopp
 */
public interface IRequestableComponent
{
	/**
	 * Gets this component's path.
	 * 
	 * @return Colon separated path to this component in the component hierarchy
	 */
	String getPageRelativePath();

	/**
	 * Gets the id of this component.
	 * 
	 * @return The id of this component
	 */
	String getId();

	/**
	 * Returns page this component belongs to.
	 * 
	 * @return page instance or <code>null</code>
	 */
	IRequestablePage getPage();

	/**
	 * Gets the component at the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	IRequestableComponent get(String path);

	/**
	 * Gets a stable id for the specified behavior. The id remains stable from the point this method
	 * is first called for the behavior until the behavior has been removed from the component
	 * 
	 * @param behavior
	 * @return a stable id for the specified behavior
	 */
	int getBehaviorId(Behavior behavior);

	/**
	 * Gets the behavior for the specified id
	 * 
	 * @param id
	 * @return behavior or {@code null} if none
	 * @throws InvalidBehaviorIdException
	 *             when behavior with this id cannot be found
	 */
	Behavior getBehaviorById(int id);

	/**
	 * Detaches the component.
	 * <p>
	 * NOTE: this method is not inherited from {@link IDetachable} on purpose. in Wicket the
	 * assumption for a long time has been that {@link Component}s do not implement
	 * {@link IDetachable}; doing so may lead to some very nasty side-effects. Consider
	 * {@code AbstractPropertyModel#detach()} which looks like this:
	 * 
	 * <pre>
	 * public void detach()
	 * {
	 * 	// Detach nested object if it's a detachable
	 * 	if (target instanceof IDetachable)
	 * 	{
	 * 		((IDetachable)target).detach();
	 * 	}
	 * }
	 * </pre>
	 * 
	 * If the model was constructed thusly, which is quite common: {@code new PropertyModel(this,
	 * "person")} and {@link Component} implemented {@link IDetachable} then calling @{code
	 * model.detach()} will cause an infinite loop with the model trying to detach the component and
	 * the component trying to detach the model.
	 * 
	 * </p>
	 */
	void detach();
}
