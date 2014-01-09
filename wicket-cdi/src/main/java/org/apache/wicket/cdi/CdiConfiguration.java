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
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.util.lang.Args;
import org.jboss.seam.conversation.spi.SeamConversationContextFactory;

/**
 * Configures CDI integration
 * 
 * @author igor
 * 
 */
public class CdiConfiguration
{
	private BeanManager beanManager;
	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;
	private INonContextualManager nonContextualManager;

	private boolean injectComponents = true;
	private boolean injectApplication = true;
	private boolean injectSession = true;
	private boolean injectBehaviors = true;
	private boolean autoConversationManagement = false;

	/**
	 * Constructor
	 * 
	 * @param beanManager
	 */
	public CdiConfiguration(BeanManager beanManager)
	{
		Args.notNull(beanManager, "beanManager");

		this.beanManager = beanManager;
		nonContextualManager = new NonContextualManager(beanManager);
	}

	/**
	 * Gets the configured bean manager
	 * 
	 * @return bean manager or {@code null} if none
	 */
	public BeanManager getBeanManager()
	{
		return beanManager;
	}

	public IConversationPropagation getPropagation()
	{
		return propagation;
	}

	/**
	 * Checks if auto conversation management is enabled. See
	 * {@link #setAutoConversationManagement(boolean)} for details.
	 */
	public boolean isAutoConversationManagement()
	{
		return autoConversationManagement;
	}

	/**
	 * Toggles automatic conversation management feature.
	 * 
	 * Automatic conversation management controls the lifecycle of the conversation based on
	 * presence of components implementing the {@link ConversationalComponent} interface. If such
	 * components are found in the page a conversation is marked persistent, and if they are not the
	 * conversation is marked transient. This greatly simplifies the management of conversation
	 * lifecycle.
	 * 
	 * Sometimes it is necessary to manually control the application. For these cases, once a
	 * conversation is started {@link AutoConversation} bean can be used to mark the conversation as
	 * manually-managed.
	 * 
	 * @param enabled
	 * 
	 * @return {@code this} for easy chaining
	 */
	public CdiConfiguration setAutoConversationManagement(boolean enabled)
	{
		autoConversationManagement = enabled;
		return this;
	}

	public CdiConfiguration setPropagation(IConversationPropagation propagation)
	{
		this.propagation = propagation;
		return this;
	}

	public INonContextualManager getNonContextualManager()
	{
		return nonContextualManager;
	}

	public CdiConfiguration setNonContextualManager(INonContextualManager nonContextualManager)
	{
		this.nonContextualManager = nonContextualManager;
		return this;
	}

	public boolean isInjectComponents()
	{
		return injectComponents;
	}

	public CdiConfiguration setInjectComponents(boolean injectComponents)
	{
		this.injectComponents = injectComponents;
		return this;
	}

	public boolean isInjectApplication()
	{
		return injectApplication;
	}

	public CdiConfiguration setInjectApplication(boolean injectApplication)
	{
		this.injectApplication = injectApplication;
		return this;
	}

	public boolean isInjectSession()
	{
		return injectSession;
	}

	public CdiConfiguration setInjectSession(boolean injectSession)
	{
		this.injectSession = injectSession;
		return this;
	}

	public boolean isInjectBehaviors()
	{
		return injectBehaviors;
	}

	public CdiConfiguration setInjectBehaviors(boolean injectBehaviors)
	{
		this.injectBehaviors = injectBehaviors;
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
			throw new IllegalStateException(
				"Configuration does not have a BeanManager instance configured");
		}

		CdiContainer container = new CdiContainer(beanManager, nonContextualManager);
		container.bind(application);

		RequestCycleListenerCollection listeners = new RequestCycleListenerCollection();
		application.getRequestCycleListeners().add(listeners);

		// enable conversation propagation
		if (getPropagation() != ConversationPropagation.NONE)
		{
			listeners.add(new ConversationPropagator(application, container, getPropagation(),
				autoConversationManagement));
			application.getComponentPreOnBeforeRenderListeners().add(
				new ConversationExpiryChecker(container));
			SeamConversationContextFactory.setDisableNoopInstance(true);
		}

		// enable detach event
		listeners.add(new DetachEventEmitter(container));


		// inject application instance
		if (isInjectApplication())
		{
			container.getNonContextualManager().postConstruct(application);
		}

		// enable injection of various framework components

		if (isInjectSession())
		{
			application.getSessionListeners().add(new SessionInjector(container));
		}

		if (isInjectComponents())
		{
			application.getComponentInstantiationListeners().add(new ComponentInjector(container));
		}

		if (isInjectBehaviors())
		{
			application.getBehaviorInstantiationListeners().add(new BehaviorInjector(container));
		}

		// enable cleanup

		application.getApplicationListeners().add(
			new CdiShutdownCleaner(beanManager, isInjectApplication()));

		return container;
	}

}
