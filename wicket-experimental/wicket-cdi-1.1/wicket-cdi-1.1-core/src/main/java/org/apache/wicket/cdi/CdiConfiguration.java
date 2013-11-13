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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures CDI integration
 * 
 * @author igor
 * @author jsarman
 */
@ApplicationScoped
public class CdiConfiguration
{
	private static final Logger logger = LoggerFactory.getLogger(CdiConfiguration.class);

	@Inject
	AbstractCdiContainer container;

	@Inject
	INonContextualManager nonContextualManager;

	@Inject
	ConversationPropagator conversationPropagator;

	@Inject
	ConversationExpiryChecker conversationExpiryChecker;

	@Inject
	DetachEventEmitter detachEventEmitter;

	@Inject
	BehaviorInjector behaviorInjector;

	@Inject
	ComponentInjector componentInjector;

	@Inject
	SessionInjector sessionInjector;

	@Inject
	ConversationManager conversationManager;

	protected Map<String, ConfigurationParameters> parameters;

	/**
	 * Not intended for public use. Use {@link #get()}
	 */
	public CdiConfiguration()
	{
	}


	@PostConstruct
	public void init()
	{
		parameters = new TreeMap<String, ConfigurationParameters>();
	}

	public boolean isInjectComponents()
	{
		return getApplicationParameters().isInjectComponents();
	}

	/**
	 * Flag to set if ComponentInjection is enabled.
	 * <p/>
	 * This method will throw IllegalStateException if called after configured.
	 * 
	 * @param injectComponents
	 * @return {@code this} for easy chaining
	 * @deprecated Application Level Configuration replaced with
	 *             {@link CdiWicketFilter}
	 */
	@Deprecated
	public CdiConfiguration setInjectComponents(boolean injectComponents)
	{
		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			throw new IllegalStateException(
					"Component Injection can only be changed before configure is called");
		}
		params.setInjectComponents(injectComponents);
		return this;
	}

	public boolean isInjectApplication()
	{
		return getApplicationParameters().isInjectApplication();
	}

	/**
	 * Flag to set if ApplicationInjection is enabled.
	 * <p/>
	 * This method will throw IllegalStateException if called after configured.
	 * 
	 * @param injectApplication
	 * @return {@code this} for easy chaining
	 * @deprecated Application Level Configuration replaced with
	 *             {@link CdiWicketFilter}
	 */
	@Deprecated
	public CdiConfiguration setInjectApplication(boolean injectApplication)
	{
		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			throw new IllegalStateException(
					"Application Injection can only be changed before configure is called");
		}
		params.setInjectApplication(injectApplication);
		return this;
	}

	public boolean isInjectSession()
	{
		return getApplicationParameters().isInjectSession();
	}

	/**
	 * Flag to set if SessionInjection is enabled.
	 * <p/>
	 * This method will throw IllegalStateException if called after configured.
	 * 
	 * @param injectSession
	 * @return {@code this} for easy chaining
	 * @deprecated Application Level Configuration replaced with
	 *             {@link CdiWicketFilter}
	 */
	@Deprecated
	public CdiConfiguration setInjectSession(boolean injectSession)
	{
		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			throw new IllegalStateException(
					"Session Injection can only be changed before configure is called");
		}
		params.setInjectSession(injectSession);
		return this;
	}

	public boolean isInjectBehaviors()
	{
		return getApplicationParameters().isInjectBehaviors();
	}

	/**
	 * Flag to set if BehaviorInjection is enabled.
	 * <p/>
	 * This method will throw IllegalStateException if called after configured.
	 * 
	 * @param injectBehaviors
	 * @return {@code this} for easy chaining
	 * @deprecated Application Level Configuration replaced with
	 *             {@link CdiWicketFilter}
	 */
	@Deprecated
	public CdiConfiguration setInjectBehaviors(boolean injectBehaviors)
	{
		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			throw new IllegalStateException(
					"Behavior Injection can only be changed before configure is called");
		}
		params.setInjectBehaviors(injectBehaviors);
		return this;
	}


	public @Produces
	@Propagation
	IConversationPropagation getPropagation()
	{
		return getApplicationParameters().getPropagation();
	}


	public @Produces
	@Auto
	Boolean isAutoConversationManagement()
	{
		return getApplicationParameters().isAutoConversationManagement();
	}

	/**
	 * Toggles automatic conversation management feature.
	 * <p/>
	 * Automatic conversation management controls the lifecycle of the
	 * conversation based on presence of components implementing the
	 * {@link ConversationalComponent} interface. If such components are found
	 * in the page a conversation is marked persistent, and if they are not the
	 * conversation is marked transient. This greatly simplifies the management
	 * of conversation lifecycle.
	 * <p/>
	 * ConversationManagement can also be enable per Conversation after
	 * configured. Once the CdiConfiguration is configured this call is passed
	 * to {@link ConversationManager#setManageConversation(java.lang.Boolean) }
	 * for the ConversationManager in the current ConversationScope. This allows
	 * for ConversationManagement per active Conversation.
	 * <p/>
	 * 
	 * @param enabled
	 * @return {@code this} for easy chaining
	 */
	public CdiConfiguration setAutoConversationManagement(boolean enabled)
	{
		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			if (container.getCurrentConversation().isTransient())
			{
				logger.warn("Not setting AutoConversationManagement because the conversation context is transient.");
				return this;
			}
			conversationManager.setManageConversation(enabled);
		}
		else
		{
			params.setAutoConversationManagement(enabled);
		}
		return this;
	}

	/**
	 * Method to set the ConversationPropagation.
	 * <p/>
	 * 
	 * @param propagation
	 * @return {@code this} for easy chaining
	 */
	public CdiConfiguration setPropagation(IConversationPropagation propagation)
	{
		Args.notNull(propagation, "propagation");

		ConfigurationParameters params = getApplicationParameters();
		if (params.isConfigured())
		{
			if (container.getCurrentConversation().isTransient())
			{
				logger.warn("Not setting propagation because the conversation context is transient.");
				return this;
			}
			conversationManager.setPropagation(propagation);
		}
		else
		{
			params.setPropagation(propagation);
		}
		return this;
	}

	public INonContextualManager getNonContextualManager()
	{
		return nonContextualManager;
	}

	/**
	 * @return true if configured for Application
	 */
	public boolean isConfigured()
	{
		return getApplicationParameters().isConfigured();
	}

	protected ConfigurationParameters getApplicationParameters()
	{
		ConfigurationParameters params = parameters.get(Application.get().getApplicationKey());
		if (params == null)
		{
			try
			{
				Application app = Application.get();
				if (app.getApplicationKey() == null)
				{
					throw new WicketRuntimeException();
				}
				params = new ConfigurationParameters();
				parameters.put(app.getApplicationKey(), params);
			}
			catch (WicketRuntimeException wre)
			{
				throw new IllegalStateException("Application is not ready.");
			}
		}
		return params;
	}

	/**
	 * Configures the specified application. This method allows for
	 * CdiConfiguration to be setup at the Application Level. Use the
	 * {@link CdiWicketFilter} as the filterClass or add the
	 * {@link CdiWebApplicationFactory} to the Standard WicketFilter with
	 * init-param applicationFactoryClassName for setup during Application
	 * Initialization. This allows for Injected classes in the WebApplication to
	 * be ready before init() is called.
	 * 
	 * @param application
	 * @return
	 * @deprecated Application Level Configuration replaced with
	 *             {@link CdiWicketFilter}
	 */
	@Deprecated
	public void configure(Application application)
	{
		ConfigurationParameters params = getApplicationParameters();
		configure(application.getApplicationKey(), application, params);
	}

	protected synchronized void configure(String appKey, Application application,
			ConfigurationParameters params)
	{

		if (parameters.containsKey(appKey))
		{
			params = parameters.get(appKey);
			if (params.isConfigured())
			{
				throw new IllegalStateException("Cannot configure CdiConfiguration multiple times");
			}
		}
		else
		{
			parameters.put(appKey, params);
		}

		RequestCycleListenerCollection listeners = new RequestCycleListenerCollection();
		application.getRequestCycleListeners().add(listeners);

		// enable conversation propagation
		if (params.getPropagation() != ConversationPropagation.NONE)
		{
			enablePropagation(params, application);
		}

		// enable detach event
		listeners.add(detachEventEmitter);


		// inject application instance
		if (params.isInjectApplication())
		{
			nonContextualManager.postConstruct(application);
		}

		// enable injection of various framework components

		if (params.isInjectSession())
		{
			application.getSessionListeners().add(sessionInjector);
		}

		if (params.isInjectComponents())
		{
			application.getComponentInstantiationListeners().add(componentInjector);
		}

		if (params.isInjectBehaviors())
		{
			application.getBehaviorInstantiationListeners().add(behaviorInjector);
		}

		// enable cleanup

		application.getApplicationListeners().add(
				new CdiShutdownCleaner(params.isInjectApplication()));


		params.setConfigured(true);
	}

	/**
	 * Convenience Method to get an Injected Instance of CdiConfiguration
	 * programmatically.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CdiConfiguration get()
	{
		BeanManager beanManager = BeanManagerLookup.lookup();
		Iterator<Bean<?>> iter = beanManager.getBeans(CdiConfiguration.class).iterator();
		if (!iter.hasNext())
		{
			throw new IllegalStateException("CDI BeanManager cannot find CdiConfiguration");
		}
		Bean<CdiConfiguration> bean = (Bean<CdiConfiguration>)iter.next();
		CreationalContext<CdiConfiguration> ctx = beanManager.createCreationalContext(bean);
		return (CdiConfiguration)beanManager.getReference(bean, CdiConfiguration.class, ctx);
	}

	private void enablePropagation(ConfigurationParameters params, Application application)
	{
		disablePropagation(params); // Force remove active listeners if any
		IRequestCycleListener requestCycleListener = conversationPropagator;// new
																			// RequestCycleListenerWrapper();
		application.getRequestCycleListeners().add(requestCycleListener);
		params.setActiveRequestCycleListener(requestCycleListener);

		IComponentOnBeforeRenderListener componentOnBeforeRenderListener = new ComponentOnBeforeRenderListenerWrapper();
		application.getComponentPreOnBeforeRenderListeners().add(componentOnBeforeRenderListener);
		params.setActiveComponentOnBeforeRenderListener(componentOnBeforeRenderListener);
	}

	private void disablePropagation(ConfigurationParameters params)
	{
		IRequestCycleListener requestCycleListener = params.getActiveRequestCycleListener();
		if (requestCycleListener != null)
		{
			Application.get().getRequestCycleListeners().remove(requestCycleListener);
			params.setActiveRequestCycleListener(null);
		}
		IComponentOnBeforeRenderListener componentOnBeforeRenderListener = params
				.getActiveComponentOnBeforeRenderListener();
		if (componentOnBeforeRenderListener != null)
		{
			Application.get().getComponentPreOnBeforeRenderListeners()
					.remove(componentOnBeforeRenderListener);
			params.setActiveComponentOnBeforeRenderListener(null);
		}
	}

	/**
	 * Wrapper for the Current ConversationPropagator which allows the removal
	 * of the listener.
	 */
	class RequestCycleListenerWrapper extends AbstractRequestCycleListener
	{

		@Override
		public void onEndRequest(RequestCycle cycle)
		{
			conversationPropagator.onEndRequest(cycle);
		}

		@Override
		public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler)
		{
			conversationPropagator.onRequestHandlerScheduled(cycle, handler);
		}

		@Override
		public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler)
		{
			conversationPropagator.onRequestHandlerResolved(cycle, handler);
		}

		@Override
		public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler)
		{
			conversationPropagator.onRequestHandlerExecuted(cycle, handler);
		}

		@Override
		public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url)
		{
			conversationPropagator.onUrlMapped(cycle, handler, url);
		}

	}

	class ComponentOnBeforeRenderListenerWrapper implements IComponentOnBeforeRenderListener
	{

		@Override
		public void onBeforeRender(Component component)
		{
			conversationExpiryChecker.onBeforeRender(component);
		}

	}

}
