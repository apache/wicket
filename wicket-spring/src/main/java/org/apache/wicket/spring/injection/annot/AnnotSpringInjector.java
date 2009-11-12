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
package org.apache.wicket.spring.injection.annot;

import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.spring.ISpringContextLocator;

/**
 * Injector that injects classes based on {@link SpringBean} annotation
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AnnotSpringInjector extends ConfigurableInjector
{

	IFieldValueFactory factory;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            spring context locator
	 */
	public AnnotSpringInjector(ISpringContextLocator locator)
	{
		this(locator, true);
	}


	/**
	 * Constructor
	 * 
	 * @param locator
	 *            spring context locator
	 * @param wrapInProxies
	 *            whether or not wicket should wrap dependencies with specialized proxies that can
	 *            be safely serialized. in most cases this should be set to true.
	 */
	public AnnotSpringInjector(ISpringContextLocator locator, boolean wrapInProxies)
	{
		initFactory(locator, wrapInProxies);
	}

	private void initFactory(ISpringContextLocator locator, boolean wrapInProxies)
	{
		factory = new AnnotProxyFieldValueFactory(locator, wrapInProxies);
	}

	@Override
	protected IFieldValueFactory getFieldValueFactory()
	{
		return factory;
	}

}
