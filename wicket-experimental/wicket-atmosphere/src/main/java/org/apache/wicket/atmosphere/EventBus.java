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
package org.apache.wicket.atmosphere;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.atmosphere.config.AtmosphereParameters;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Response;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Broadcasts events to methods on components annotated with {@link Subscribe}.
 * {@linkplain EventBus#post(Object) Posted} events are broadcasted to all components on active
 * pages if they have a method annotated with {@link Subscribe}. To create and register an
 * {@code EventBus}, put the following code in your application's init method:
 *
 * <pre>
 * this.eventBus = new EventBus(this);
 * </pre>
 *
 * The {@code EventBus} will register itself in the application once instantiated. It might be
 * practical to keep a reference in the application, but you can always get it using {@link #get()}.
 *
 * @author papegaaij
 */
public class EventBus implements UnboundListener
{
	private static final Logger log = LoggerFactory.getLogger(EventBus.class);

	private static final MetaDataKey<EventBus> EVENT_BUS_KEY = new MetaDataKey<EventBus>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @return the {@code EventBus} registered for the current Wicket application.
	 */
	public static EventBus get()
	{
		return get(Application.get());
	}

	/**
	 * @param application
	 * @return the {@code EventBus} registered for the given Wicket application.
	 */
	public static EventBus get(Application application)
	{
		EventBus eventBus = application.getMetaData(EVENT_BUS_KEY);
		if (eventBus == null)
		{
			throw new WicketRuntimeException(
				"There is no EventBus registered for the given application: " +
					application.getName());
		}
		return eventBus;
	}

	private WebApplication application;

	private Broadcaster broadcaster;

	private Multimap<PageKey, EventSubscription> subscriptions = HashMultimap.create();

	private Map<String, PageKey> trackedPages = Maps.newHashMap();

	private List<ResourceRegistrationListener> registrationListeners = new CopyOnWriteArrayList<ResourceRegistrationListener>();

	private AtmosphereParameters parameters = new AtmosphereParameters();

	/**
	 * Creates and registers an {@code EventBus} for the given application. The first broadcaster
	 * returned by the {@code BroadcasterFactory} is used.
	 *
	 * @param application
	 */
	public EventBus(WebApplication application)
	{
		this(application, lookupDefaultBroadcaster());
	}

	private static Broadcaster lookupDefaultBroadcaster()
	{
		BroadcasterFactory factory = BroadcasterFactory.getDefault();
		if (factory == null)
		{
			throw new WicketRuntimeException(
				"There is no Atmosphere BroadcasterFactory configured. Did you include the "
					+ "atmosphere.xml configuration file and configured AtmosphereServlet?");
		}
		Collection<Broadcaster> allBroadcasters = factory.lookupAll();
		if (allBroadcasters.isEmpty())
		{
			throw new WicketRuntimeException(
				"The Atmosphere BroadcasterFactory has no Broadcasters, "
					+ "something is wrong with your Atmosphere configuration.");
		}
		return allBroadcasters.iterator().next();
	}

	/**
	 * Creates and registers an {@code EventBus} for the given application and broadcaster
	 *
	 * @param application
	 * @param broadcaster
	 */
	public EventBus(WebApplication application, Broadcaster broadcaster)
	{
		this.application = application;
		this.broadcaster = broadcaster;
		application.setMetaData(EVENT_BUS_KEY, this);
		application.mount(new AtmosphereRequestMapper(createEventSubscriptionInvoker()));
		application.getComponentPostOnBeforeRenderListeners().add(
			createEventSubscriptionCollector());
		application.getSessionStore().registerUnboundListener(this);
	}

	/**
	 *
	 * @return event subscription invoker
	 */
	protected EventSubscriptionInvoker createEventSubscriptionInvoker()
	{
		return new SubscribeAnnotationEventSubscriptionInvoker();
	}

	/**
	 *
	 * @return event subscription collector
	 */
	protected IComponentOnBeforeRenderListener createEventSubscriptionCollector()
	{
		return new AtmosphereEventSubscriptionCollector(this);
	}

	/**
	 * @return The {@link Broadcaster} used by the {@code EventBus} to broadcast messages to.
	 */
	public Broadcaster getBroadcaster()
	{
		return broadcaster;
	}

	/**
	 * Returns the {@linkplain AtmosphereParameters parameters} that will be passed to the
	 * Atmosphere JQuery plugin. You can change these parameters, for example to disable WebSockets.
	 *
	 * @return The parameters.
	 */
	public AtmosphereParameters getParameters()
	{
		return parameters;
	}

	/**
	 * Registers a page for the given tracking-id in the {@code EventBus}.
	 *
	 * @param trackingId
	 * @param page
	 */
	public synchronized void registerPage(String trackingId, Page page)
	{
		PageKey oldPage = trackedPages.remove(trackingId);
		PageKey pageKey = new PageKey(page.getPageId(), Session.get().getId());
		if (oldPage != null && !oldPage.equals(pageKey))
		{
			subscriptions.removeAll(oldPage);
			fireUnregistration(trackingId);
		}
		trackedPages.put(trackingId, pageKey);
		fireRegistration(trackingId, page);

		if (log.isDebugEnabled())
		{
			log.debug("registered page {} for session {}", pageKey.getPageId(),
				pageKey.getSessionId());
		}
	}

	/**
	 * Registers an {@link EventSubscription} for the given page.
	 *
	 * @param page
	 * @param subscription
	 */
	public synchronized void register(Page page, EventSubscription subscription)
	{
		if (log.isDebugEnabled())
		{
			log.debug(
				"registering {} for page {} for session {}: {}{}",
				new Object[] {
						subscription.getBehaviorIndex() == null ? "component" : "behavior",
						page.getPageId(),
						Session.get().getId(),
						subscription.getComponentPath(),
						subscription.getBehaviorIndex() == null ? "" : ":" +
							subscription.getBehaviorIndex() });
		}
		PageKey pageKey = new PageKey(page.getPageId(), Session.get().getId());
		if (!subscriptions.containsEntry(pageKey, subscription))
		{
			subscriptions.put(pageKey, subscription);
		}
	}

	/**
	 * Unregisters an {@link EventSubscription} for the given page.
	 *
	 * @param page
	 * @param subscription
	 */
	public synchronized void unregister(Page page, EventSubscription subscription)
	{
		if (log.isDebugEnabled())
		{
			log.debug(
				"unregistering {} for page {} for session {}: {}{}",
				new Object[] {
						subscription.getBehaviorIndex() == null ? "component" : "behavior",
						page.getPageId(),
						Session.get().getId(),
						subscription.getComponentPath(),
						subscription.getBehaviorIndex() == null ? "" : ":" +
							subscription.getBehaviorIndex() });
		}
		PageKey pageKey = new PageKey(page.getPageId(), Session.get().getId());
		subscriptions.remove(pageKey, subscription);
	}

	/**
	 * Unregisters all {@link EventSubscription}s for the given component, including the
	 * subscriptions for its behaviors.
	 *
	 * @param page
	 * @param subscription
	 */
	public synchronized void unregister(Component component)
	{
		String componentPath = component.getPageRelativePath();
		PageKey pageKey = new PageKey(component.getPage().getPageId(), Session.get().getId());
		Collection<EventSubscription> subscriptionsForPage = subscriptions.get(pageKey);
		Iterator<EventSubscription> it = subscriptionsForPage.iterator();
		while (it.hasNext())
		{
			if (it.next().getComponentPath().equals(componentPath))
				it.remove();
		}
	}

	/**
	 * Unregisters all subscriptions for the given tracking id.
	 *
	 * @param trackingId
	 */
	public synchronized void unregisterConnection(String trackingId)
	{
		PageKey pageKey = trackedPages.remove(trackingId);
		if (pageKey != null)
		{
			fireUnregistration(trackingId);
			if (log.isDebugEnabled())
			{
				log.debug("unregistering page {} for session {}", pageKey.getPageId(),
					pageKey.getSessionId());
			}
		}
	}

	/**
	 * Post an event to a single resource. This will invoke the event handlers on all components on
	 * the page with the suspended connection. The resulting AJAX update (if any) is pushed to the
	 * client. You can find the UUID via {@link AtmosphereBehavior#getUUID(Page)}. If no resource
	 * exists with the given UUID, no post is performed.
	 *
	 * @param event
	 * @param resourceUuid
	 */
	public void post(Object event, String resourceUuid)
	{
		AtmosphereResource resource = AtmosphereResourceFactory.getDefault().find(resourceUuid);
		if (resource != null)
		{
			post(event, resource);
		}
	}

	/**
	 * Post an event to a single resource. This will invoke the event handlers on all components on
	 * the page with the suspended connection. The resulting AJAX update (if any) is pushed to the
	 * client.
	 *
	 * @param event
	 * @param resource
	 */
	public void post(Object event, AtmosphereResource resource)
	{
		ThreadContext oldContext = ThreadContext.get(false);
		try
		{
			postToSingleResource(event, resource);
		}
		finally
		{
			ThreadContext.restore(oldContext);
		}
	}

	/**
	 * Post an event to all pages that have a suspended connection. This will invoke the event
	 * handlers on components, annotated with {@link Subscribe}. The resulting AJAX updates are
	 * pushed to the clients.
	 *
	 * @param event
	 */
	public void post(Object event)
	{
		ThreadContext oldContext = ThreadContext.get(false);
		try
		{
			for (AtmosphereResource resource : broadcaster.getAtmosphereResources())
			{
				postToSingleResource(event, resource);
			}
		}
		finally
		{
			ThreadContext.restore(oldContext);
		}
	}

	private void postToSingleResource(Object payload, AtmosphereResource resource)
	{
		AtmosphereEvent event = new AtmosphereEvent(payload, resource);
		ThreadContext.detach();
		ThreadContext.setApplication(application);
		PageKey key;
		Collection<EventSubscription> subscriptionsForPage;
		synchronized (this)
		{
			key = trackedPages.get(resource.uuid());
			subscriptionsForPage = Collections2.filter(
				Collections.unmodifiableCollection(subscriptions.get(key)), new EventFilter(event));
		}
		if (key == null)
			broadcaster.removeAtmosphereResource(resource);
		else if (!subscriptionsForPage.isEmpty())
			post(resource, key, subscriptionsForPage, event);
	}

	private void post(AtmosphereResource resource, PageKey pageKey,
		Collection<EventSubscription> subscriptionsForPage, AtmosphereEvent event)
	{
		String filterPath = WebApplication.get()
			.getWicketFilter()
			.getFilterConfig()
			.getInitParameter(WicketFilter.FILTER_MAPPING_PARAM);
		filterPath = filterPath.substring(1, filterPath.length() - 1);
		HttpServletRequest httpRequest = new HttpServletRequestWrapper(resource.getRequest())
		{
			@Override
			public String getContextPath()
			{
				String ret = super.getContextPath();
				return ret == null ? "" : ret;
			}
		};
		AtmosphereWebRequest request = new AtmosphereWebRequest(
			(ServletWebRequest)application.newWebRequest(httpRequest, filterPath), pageKey,
			subscriptionsForPage, event);
		Response response = new AtmosphereWebResponse(resource.getResponse());
		if (application.createRequestCycle(request, response).processRequestAndDetach())
			broadcaster.broadcast(response.toString(), resource);
	}

	@Override
	public synchronized void sessionUnbound(String sessionId)
	{
		log.debug("Session unbound {}", sessionId);
		Iterator<Entry<String, PageKey>> pageIt = trackedPages.entrySet().iterator();
		while (pageIt.hasNext())
		{
			Entry<String, PageKey> curEntry = pageIt.next();
			if (curEntry.getValue().isForSession(sessionId))
			{
				pageIt.remove();
				fireUnregistration(curEntry.getKey());
			}
		}
		Iterator<PageKey> subscriptionIt = subscriptions.keySet().iterator();
		while (subscriptionIt.hasNext())
			if (subscriptionIt.next().isForSession(sessionId))
				subscriptionIt.remove();
	}

	/**
	 * Add a new {@link ResourceRegistrationListener} to the {@code EventBus}. This listener will be
	 * notified on all Atmosphere resource registrations and unregistrations.
	 *
	 * @param listener
	 */
	public void addRegistrationListener(ResourceRegistrationListener listener)
	{
		registrationListeners.add(listener);
	}

	/**
	 * Removes a previously added {@link ResourceRegistrationListener}.
	 *
	 * @param listener
	 */
	public void removeRegistrationListener(ResourceRegistrationListener listener)
	{
		registrationListeners.add(listener);
	}

	private void fireRegistration(String uuid, Page page)
	{
		for (ResourceRegistrationListener curListener : registrationListeners)
		{
			curListener.resourceRegistered(uuid, page);
		}
	}

	private void fireUnregistration(String uuid)
	{
		for (ResourceRegistrationListener curListener : registrationListeners)
		{
			curListener.resourceUnregistered(uuid);
		}
	}
}
