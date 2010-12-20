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
package org.apache.wicket.examples.resourcedecoration;

import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * Assigns a group and order to load in that group to a {@link PackageResourceReference}.
 * 
 * @author jthomerson
 */
public class GroupedAndOrderedResourceReference extends PackageResourceReference
{

	private static final long serialVersionUID = 1L;

	/**
	 * Some demo groups
	 */
	public static enum ResourceGroup {
		UNKNOWN, GLOBAL, APPLICATION, PAGE, COMPONENT
	}

	private final ResourceGroup group;
	private final int loadOrder;

	/**
	 * Construct.
	 * 
	 * @param group
	 * @param loadOrder
	 * @param scope
	 * @param name
	 */
	public GroupedAndOrderedResourceReference(ResourceGroup group, int loadOrder, Class<?> scope,
		String name)
	{
		super(scope, name);
		this.group = group;
		this.loadOrder = loadOrder;
	}

	/**
	 * Construct.
	 * 
	 * @param group
	 * @param loadOrder
	 * @param name
	 */
	public GroupedAndOrderedResourceReference(ResourceGroup group, int loadOrder, String name)
	{
		super(name);
		this.group = group;
		this.loadOrder = loadOrder;
	}

	/**
	 * @return the assigned group for this {@link PackageResourceReference}
	 */
	public ResourceGroup getGroup()
	{
		return group;
	}

	/**
	 * @return the load order for this {@link PackageResourceReference}
	 */
	public int getLoadOrder()
	{
		return loadOrder;
	}
}
