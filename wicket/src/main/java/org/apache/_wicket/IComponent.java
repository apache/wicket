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
package org.apache._wicket;

import org.apache.wicket.model.IDetachable;

/**
 * Base interface for components. The purpose of this interface is to make certain parts of Wicket
 * easier to mock and unit test.
 * 
 * @author Matej Knopp
 */
// TODO: Better name would be nice
public interface IComponent extends IDetachable
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
	 * Retrieves id by which this component is represented within the markup. This is either the id
	 * attribute set explicitly via a call to {@link #setMarkupId(String)}, id attribute defined in
	 * the markup, or an automatically generated id - in that order.
	 * <p>
	 * If no id is set and <code>createIfDoesNotExist</code> is false, this method will return
	 * null. Otherwise it will generate an id value that will be unique in the page. This is the
	 * preferred way as there is no chance of id collision.
	 * <p>
	 * Note: This method should only be called after the component or its parent have been added to
	 * the page.
	 * 
	 * @param createIfDoesNotExist
	 *            When there is no existing markup id, determines whether it should be generated or
	 *            whether <code>null</code> should be returned.
	 * 
	 * @return markup id of the component
	 */
	public String getMarkupId(boolean createIfDoesNotExist);
	
	/**
	 * Gets the component at the given path.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 */
	public IComponent get(String path);
}
