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

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;

/**
 * Configures CDI integration
 * 
 * @author igor
 * 
 */
public class CdiConfiguration
{
	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;

	private boolean injectComponents = true;
	private boolean injectApplication = true;
	private boolean injectSession = true;
	private boolean injectBehaviors = true;
	private boolean autoConversationManagement = false;

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
	 * Automatic conversation management controls the lifecycle of the
	 * conversation based on presence of components implementing the
	 * {@link ConversationalComponent} interface. If such components are found
	 * in the page a conversation is marked persistent, and if they are not the
	 * conversation is marked transient. This greatly simplifies the management
	 * of conversation lifecycle.
	 * 
	 * Sometimes it is necessary to manually control the application. For these
	 * cases, once a conversation is started {@link AutoConversation} bean can
	 * be used to mark the conversation as manually-managed.
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
	public AbstractCdiContainer configure(Application application, AbstractCdiContainer container)
	{
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
		}

		// enable detach event
		listeners.add(new DetachEventEmitter(container));


		// inject application instance
		if (isInjectApplication())
		{
			NonContextual.of(application.getClass()).postConstruct(application);
		}

		// enable injection of various framework components

		if (isInjectSession())
		{
			application.getSessionListeners().add(new SessionInjector());
		}

		if (isInjectComponents())
		{
			application.getComponentInstantiationListeners().add(new ComponentInjector());
		}

		if (isInjectBehaviors())
		{
			application.getBehaviorInstantiationListeners().add(new BehaviorInjector());
		}

		// enable cleanup

		application.getApplicationListeners().add(new CdiShutdownCleaner(isInjectApplication()));

		return container;
	}

}
