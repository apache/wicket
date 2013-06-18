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
package org.apache.wicket.authorization.strategies;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;


/**
 * Compound implementation of the IAuthorizationStrategy that lets you chain two or more strategies
 * together.
 * 
 * @author ivaynberg
 */
public class CompoundAuthorizationStrategy implements IAuthorizationStrategy
{
	/** List of strategies to consult */
	private final List<IAuthorizationStrategy> strategies = new ArrayList<>();

	/**
	 * Adds a strategy to the chain
	 * 
	 * @param strategy
	 *            Strategy to add
	 */
	public final void add(IAuthorizationStrategy strategy)
	{
		if (strategy == null)
		{
			throw new IllegalArgumentException("Strategy argument cannot be null");
		}
		strategies.add(strategy);
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	@Override
	public final <T extends IRequestableComponent> boolean isInstantiationAuthorized(
		Class<T> componentClass)
	{
		for (IAuthorizationStrategy strategy : strategies)
		{
			if (!strategy.isInstantiationAuthorized(componentClass))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	@Override
	public final boolean isActionAuthorized(Component component, Action action)
	{
		for (IAuthorizationStrategy strategy : strategies)
		{
			if (!strategy.isActionAuthorized(component, action))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isResourceAuthorized(IResource resource, PageParameters parameters)
	{
		for (IAuthorizationStrategy strategy : strategies)
		{
			if (!strategy.isResourceAuthorized(resource, parameters))
			{
				return false;
			}
		}
		return true;
	}
}
