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

import jakarta.enterprise.inject.spi.BeanManager;
import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.util.lang.Args;

/**
 * Configures CDI integration
 * 
 * @author igor
 * 
 */
public class CdiConfiguration
{
	private static final MetaDataKey<CdiConfiguration> CDI_CONFIGURATION_KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;

	private BeanManager beanManager;

	/**
	 * Constructor
	 */
	public CdiConfiguration()
	{
	}

	public CdiConfiguration(BeanManager beanManager)
	{
		Args.notNull(beanManager, "beanManager");
		this.beanManager = beanManager;
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

	public BeanManager getBeanManager()
	{
		if (beanManager == null)
		{
			throw new IllegalStateException(
				"No BeanManager was resolved during configuration. Be sure " +
					"to specify a BeanManager in CdiConfiguration constructor or that one can be resolved by BeanManagerLookup, and that CdiConfiguration#configure is called.");
		}
		return beanManager;
	}

	/**
	 * Configures the specified application
	 * 
	 * @param application
	 */
	public void configure(Application application)
	{
		if (beanManager == null)
		{
			beanManager = BeanManagerLookup.lookup();
		}

		if (beanManager == null)
		{
			throw new IllegalStateException(
				"No BeanManager was set or found via the CDI provider. Check your CDI setup or specify a BeanManager in the CdiConfiguration.");
		}

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

		NonContextual.of(application).postConstruct(application);

		// enable injection of various framework components
		application.getSessionListeners().add(new SessionInjector());
		application.getComponentInstantiationListeners().add(new ComponentInjector());
		application.getBehaviorInstantiationListeners().add(new BehaviorInjector());

		// enable cleanup
		application.getApplicationListeners().add(new CdiShutdownCleaner());
	}

	public static CdiConfiguration get(Application application)
	{
		CdiConfiguration configuration = application.getMetaData(CDI_CONFIGURATION_KEY);
		if (configuration == null)
			throw new IllegalStateException("No CdiConfiguration is set");
		return configuration;
	}
}
