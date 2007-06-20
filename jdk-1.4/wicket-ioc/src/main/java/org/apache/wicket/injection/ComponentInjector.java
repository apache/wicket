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
package org.apache.wicket.injection;

import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.injection.web.InjectorHolder;

/**
 * Enables your application to do Spring dependency injection. To use, register
 * the injector as a {@link org.apache.wicket.application.IComponentInstantiationListener}
 * like this:
 * 
 * <pre>
 *      ... (in your application's constructor or init method)
 *      add(new ComponentInjector());
 *      ...
 * </pre>
 * 
 * @see org.apache.wicket.application.IComponentInstantiationListener
 * @see org.apache.wicket.Application#addComponentInstantiationListener(org.apache.wicket.application.IComponentInstantiationListener)
 * @author Eelco Hillenius
 */
public class ComponentInjector implements IComponentInstantiationListener
{

	/**
	 * Construct.
	 */
	public ComponentInjector()
	{
	}

	/**
	 * @see org.apache.wicket.application.IComponentInstantiationListener#onInstantiation(org.apache.wicket.Component)
	 */
	public void onInstantiation(Component component)
	{
		getInjector().inject(component);
	}

	private ConfigurableInjector getInjector()
	{
		ConfigurableInjector injector = InjectorHolder.getInjector();
		if (injector == null)
		{
			throw new RuntimeException("injector not set in InjectorHolder");
		}
		return injector;
	}
}
