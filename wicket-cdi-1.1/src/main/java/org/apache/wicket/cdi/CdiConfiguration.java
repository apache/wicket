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

import javax.enterprise.inject.spi.BeanManager;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;

/**
 * Configures CDI integration
 * 
 * @author igor
 * 
 */
public class CdiConfiguration
{
	private static final MetaDataKey<CdiConfiguration> CDI_CONFIGURATION_KEY = new MetaDataKey<CdiConfiguration>()
	{
		private static final long serialVersionUID = 1L;
	};

	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;

	private BeanManager fallbackBeanManager;

	/**
	 * Constructor
	 */
	public CdiConfiguration()
	{
	}

	public IConversationPropagation getPropagation()
	{
		return propagation;
	}

	public CdiConfiguration setPropagation(IConversationPropagation propagation)
	{
		this.propagation = propagation;
		return this;
	}

	public BeanManager getFallbackBeanManager()
	{
		return fallbackBeanManager;
	}

	/**
	 * Sets a BeanManager that should be used if all strategies to lookup a
	 * BeanManager fail. This can be used in scenarios where you do not have
	 * JNDI available and do not want to bootstrap the CDI provider. It should
	 * be noted that the fallback BeanManager can only be used within the
	 * context of a Wicket application (ie. Application.get() should return the
	 * application that was configured with this CdiConfiguration).
	 * 
	 * @param fallbackBeanManager
	 * @return
	 */
	public CdiConfiguration setFallbackBeanManager(BeanManager fallbackBeanManager)
	{
		this.fallbackBeanManager = fallbackBeanManager;
		return this;
	}

	/**
	 * Configures the specified application
	 * 
	 * @param application
	 * @return
	 */
	public void configure(Application application)
	{
		if (application.getMetaData(CDI_CONFIGURATION_KEY) != null)
		{
			throw new IllegalStateException("Cdi already configured for this application");
		}
		application.setMetaData(CDI_CONFIGURATION_KEY, this);

		RequestCycleListenerCollection listeners = new RequestCycleListenerCollection();
		application.getRequestCycleListeners().add(listeners);

		// enable conversation propagation
		if (getPropagation() != ConversationPropagation.NONE)
		{
			listeners.add(new ConversationPropagator(application, getPropagation()));
			application.getComponentPreOnBeforeRenderListeners().add(
					new AutoConversationManager(getPropagation()));
			application.getComponentPreOnBeforeRenderListeners().add(
					new ConversationExpiryChecker());
		}

		// enable detach event
		listeners.add(new DetachEventEmitter());

		NonContextual.of(application.getClass()).postConstruct(application);

		// enable injection of various framework components
		application.getSessionListeners().add(new SessionInjector());
		application.getComponentInstantiationListeners().add(new ComponentInjector());
		application.getBehaviorInstantiationListeners().add(new BehaviorInjector());

		// enable cleanup
		application.getApplicationListeners().add(new CdiShutdownCleaner());
	}

	public static CdiConfiguration get(Application application)
	{
		return application.getMetaData(CDI_CONFIGURATION_KEY);
	}
}
