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
package org.apache.wicket.cdi.weld;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import org.apache.wicket.Application;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.cdi.CdiInjector;
import org.apache.wicket.cdi.ConversationPropagation;
import org.apache.wicket.cdi.ConversationPropagator;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Args;
import org.jboss.weld.environment.servlet.Listener;

/**
 * Configures Weld integration
 * 
 * @author igor
 * 
 */
public class WeldConfiguration
{
	private BeanManager beanManager;
	private ConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;

	/**
	 * Gets the configured bean manager
	 * 
	 * @return bean manager or {@code null} if none
	 */
	public BeanManager getBeanManager()
	{
		return beanManager;
	}

	/**
	 * Sets the bean manager
	 * 
	 * @param beanManager
	 * @return
	 */
	public WeldConfiguration setBeanManager(BeanManager beanManager)
	{
		Args.notNull(beanManager, "beanManager");
		this.beanManager = beanManager;
		return this;
	}

	private WeldConfiguration resolveBeanManager(ServletContext sc)
	{
		BeanManager bm = (BeanManager)sc.getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);

		if (bm != null)
		{
			beanManager = bm;
		}
		return this;
	}

	/**
	 * Configures the specified application
	 * 
	 * @param application
	 * @return
	 */
	public CdiContainer configure(Application application)
	{
		if (beanManager == null)
		{
			resolveBeanManager(((WebApplication)application).getServletContext());
		}
		if (beanManager == null)
		{
			throw new IllegalStateException(
				"Configuration does not have a BeanManager instance configured");
		}

		WeldContainer container = new WeldContainer(beanManager);
		container.bind(application);

		application.getComponentInstantiationListeners().add(new CdiInjector(container));

		if (propagation != ConversationPropagation.NONE)
		{
			application.getRequestCycleListeners().add(
				new ConversationPropagator(container, propagation));
		}

		return container;
	}

}
