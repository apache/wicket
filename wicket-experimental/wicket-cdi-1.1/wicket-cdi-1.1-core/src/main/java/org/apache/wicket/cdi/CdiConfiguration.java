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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;

/**
 * Configures CDI integration
 *
 * @author igor
 */
@ApplicationScoped
public class CdiConfiguration
{
	private static final String[] defaultIgnoredPackages = new String[]
			{
					"org.apache.wicket.markup",
					"org.apache.wicket.protocol.http",
					"org.apache.wicket.behavior",
			};

	private IConversationPropagation propagation = ConversationPropagation.NONBOOKMARKABLE;

	@Inject
	BeanManager beanManager;

	@Inject
	INonContextualManager nonContextualManager;

	@Inject
	Instance<ConversationPropagator> conversationPropagatorSource;

	@Inject
	Instance<ConversationExpiryChecker> conversationExpiryCheckerSource;

	@Inject
	Instance<DetachEventEmitter> detachEventEmitterSource;

	@Inject
	BehaviorInjector behaviorInjector;

	@Inject
	ComponentInjector componentInjector;

	@Inject
	SessionInjector sessionInjector;

	private boolean injectComponents = true;
	private boolean injectApplication = true;
	private boolean injectSession = true;
	private boolean injectBehaviors = true;
	private boolean autoConversationManagement = false;
	private boolean configured = false;
	private Set<String> ignoredPackages;

	@PostConstruct
	public void init()
	{
		ignoredPackages = new TreeSet<>();
		ignoredPackages.addAll(Arrays.asList(defaultIgnoredPackages));
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

	public
	@Produces
	@Propagation
	IConversationPropagation getPropagation()
	{
		return propagation;
	}

	/**
	 * Checks if auto conversation management is enabled. See
	 * {@link #setAutoConversationManagement(boolean)} for details.
	 */
	public
	@Produces
	@Auto
	Boolean isAutoConversationManagement()
	{
		return autoConversationManagement;
	}

	/**
	 * Toggles automatic conversation management feature.
	 * <p/>
	 * Automatic conversation management controls the lifecycle of the conversation based on
	 * presence of components implementing the {@link ConversationalComponent} interface. If such
	 * components are found in the page a conversation is marked persistent, and if they are not the
	 * conversation is marked transient. This greatly simplifies the management of conversation
	 * lifecycle.
	 * <p/>
	 * Sometimes it is necessary to manually control the application. For these cases, once a
	 * conversation is started {@link AutoConversation} bean can be used to mark the conversation as
	 * manually-managed.
	 *
	 * @param enabled
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

	public
	@Produces
	@IgnoreList
	String[] getPackagesToIgnore()
	{
		String[] ignore = new String[ignoredPackages.size()];
		return ignoredPackages.toArray(ignore);
	}

	public CdiConfiguration addPackagesToIgnore(String... packageNames)
	{
		ignoredPackages.addAll(Arrays.asList(packageNames));
		return this;
	}

	public CdiConfiguration removePackagesToIgnore(String... packageNames)
	{
		ignoredPackages.removeAll(Arrays.asList(packageNames));
		return this;
	}

	/**
	 * Configures the specified application
	 *
	 * @param application
	 * @return
	 */
	public synchronized void configure(Application application)
	{
		if (configured)
		{
			throw new IllegalStateException("Cannot configure CdiConfiguration multiple times");
		}

		RequestCycleListenerCollection listeners = new RequestCycleListenerCollection();
		application.getRequestCycleListeners().add(listeners);

		// enable conversation propagation
		if (getPropagation() != ConversationPropagation.NONE)
		{
			listeners.add(conversationPropagatorSource.get());
			application.getComponentPreOnBeforeRenderListeners().add(
					conversationExpiryCheckerSource.get());
		}

		// enable detach event
		listeners.add(detachEventEmitterSource.get());


		// inject application instance
		if (isInjectApplication())
		{
			nonContextualManager.postConstruct(application);
		}

		// enable injection of various framework components

		if (isInjectSession())
		{
			application.getSessionListeners().add(sessionInjector);
		}

		if (isInjectComponents())
		{
			application.getComponentInstantiationListeners().add(componentInjector);
		}

		if (isInjectBehaviors())
		{
			application.getBehaviorInstantiationListeners().add(behaviorInjector);
		}

		// enable cleanup

		application.getApplicationListeners().add(
				new CdiShutdownCleaner(isInjectApplication()));

		configured = true;

	}

	public static CdiConfiguration get()
	{
		BeanManager beanManager = CDI.current().getBeanManager();
		Iterator<Bean<?>> iter = beanManager.getBeans(CdiConfiguration.class).iterator();
		if (!iter.hasNext())
		{
			throw new IllegalStateException("CDI BeanManager cannot find CdiConfiguration");
		}
		Bean<CdiConfiguration> bean = (Bean<CdiConfiguration>) iter.next();
		CreationalContext<CdiConfiguration> ctx = beanManager.createCreationalContext(bean);
		return (CdiConfiguration) beanManager.getReference(bean, CdiConfiguration.class, ctx);
	}

}
