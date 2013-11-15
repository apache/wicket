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
package org.apache.wicket.cdi;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.util.lang.Args;

/**
 * Default implementation of {@link INonContextualManager} using {@link NonContextual} helper
 * 
 * @author igor
 * 
 */
class NonContextualManager implements INonContextualManager
{
	private final BeanManager beanManager;

	/**
	 * Constructor
	 * 
	 * @param beanManager
	 */
	public NonContextualManager(BeanManager beanManager)
	{
		Args.notNull(beanManager, "beanManager");

		this.beanManager = beanManager;
	}

	/**
	 * Performs dependency injection on the noncontextual instance
	 */
	@Override
	public <T> void inject(T instance)
	{
		Args.notNull(instance, "instance");
		NonContextual.of(instance.getClass(), beanManager).inject(instance);
	}

	/**
	 * Performs dependency injection on the noncontextual instance and invokes any
	 * {@link PostConstruct} callbacks
	 */
	@Override
	public <T> void postConstruct(T instance)
	{
		Args.notNull(instance, "instance");
		NonContextual.of(instance.getClass(), beanManager).postConstruct(instance);
	}

	/**
	 * Invokes any {@link PreDestroy} callbacks and cleans up any injected dependencies
	 */
	@Override
	public <T> void preDestroy(T instance)
	{
		Args.notNull(instance, "instance");
		NonContextual.of(instance.getClass(), beanManager).preDestroy(instance);
	}

}
