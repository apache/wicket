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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.Response;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
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
		return application.getMetaData(EVENT_BUS_KEY);
	}

	private WebApplication application;

	private Broadcaster broadcaster;

	private Multimap<PageKey, EventSubscription> subscriptions = HashMultimap.create();

	private Map<String, PageKey> trackedPages = Maps.newHashMap();

	/**
	 * Creates and registers an {@code EventBus} for the given application. The first broadcaster
	 * returned by the {@code BroadcasterFactory} is used.
	 * 
	 * @param application
	 */
	public EventBus(WebApplication application)
	{
		this(application, BroadcasterFactory.getDefault().lookupAll().iterator().next());
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
		application.mount(new AtmosphereRequestMapper());
		application.getComponentPostOnBeforeRenderListeners().add(
			new AtmosphereEventSubscriptionCollector(this));
		application.getSessionStore().registerUnboundListener(this);
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
			subscriptions.removeAll(oldPage);
		trackedPages.put(trackingId, pageKey);
		log.info("registered page {} for session {}",
			new Object[] { pageKey.getPageId(), pageKey.getSessionId() });
	}

	/**
	 * Registers an {@link EventSubscription} for the given page.
	 * 
	 * @param page
	 * @param subscription
	 */
	public synchronized void register(Page page, EventSubscription subscription)
	{
		if (log.isInfoEnabled())
		{
			log.info(
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
	 * Unregisters all subscriptions for the given tracking id.
	 * 
	 * @param trackingId
	 */
	public synchronized void unregisterConnection(String trackingId)
	{
		PageKey pageKey = trackedPages.remove(trackingId);
		if (log.isInfoEnabled() && pageKey != null)
		{
			log.info("unregistering page {} for session {}", new Object[] { pageKey.getPageId(),
					pageKey.getSessionId() });
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
				ThreadContext.detach();
				ThreadContext.setApplication(application);
				PageKey key;
				Collection<EventSubscription> subscriptionsForPage;
				synchronized (this)
				{
					key = trackedPages.get(AtmosphereBehavior.getUUID(resource));
					subscriptionsForPage = Collections2.filter(
						Collections.unmodifiableCollection(subscriptions.get(key)),
						new EventFilter(event));
				}
				if (key == null)
					broadcaster.removeAtmosphereResource(resource);
				else
					post(resource, key, subscriptionsForPage, event);
			}
		}
		finally
		{
			ThreadContext.restore(oldContext);
		}
	}

	private void post(AtmosphereResource resource, PageKey pageKey,
		Collection<EventSubscription> subscriptionsForPage, Object event)
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
		AtmosphereWebRequest request = new AtmosphereWebRequest(application.newWebRequest(
			httpRequest, filterPath), pageKey, subscriptionsForPage, event);
		Response response = new AtmosphereWebResponse(resource.getResponse());
		if (application.createRequestCycle(request, response).processRequestAndDetach())
			broadcaster.broadcast(response.toString(), resource);
	}

	@Override
	public synchronized void sessionUnbound(String sessionId)
	{
		log.info("Session unbound {}", sessionId);
		Iterator<PageKey> it = Iterators.concat(trackedPages.values().iterator(),
			subscriptions.keySet().iterator());
		while (it.hasNext())
			if (it.next().isForSession(sessionId))
				it.remove();
	}
}
