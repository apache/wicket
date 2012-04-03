package org.apache.wicket.atmosphere;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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

import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;

public class EventBus implements UnboundListener
{
	private static final MetaDataKey<EventBus> EVENT_BUS_KEY = new MetaDataKey<EventBus>()
	{
		private static final long serialVersionUID = 1L;
	};

	public static EventBus get()
	{
		return Application.get().getMetaData(EVENT_BUS_KEY);
	}

	private WebApplication application;

	private Broadcaster broadcaster;

	private Multimap<PageKey, EventSubscription> subscriptions = HashMultimap.create();

	private BiMap<String, PageKey> trackedPages = HashBiMap.create();

	public EventBus(WebApplication application)
	{
		this.application = application;
		application.setMetaData(EVENT_BUS_KEY, this);
		application.mount(new PushRequestMapper());
		application.getComponentPreOnBeforeRenderListeners().add(
			new AtmosphereEventSubscriptionCollector(this));
		broadcaster = BroadcasterFactory.getDefault().lookup("/*");
	}

	public synchronized void registerPage(String trackingId, Page page)
	{
		PageKey oldPage = trackedPages.remove(trackingId);
		PageKey pageKey = new PageKey(page.getPageId(), Session.get().getId());
		if (oldPage != null && !oldPage.equals(pageKey))
			subscriptions.removeAll(oldPage);
		trackedPages.forcePut(trackingId, pageKey);
	}

	public synchronized void register(Page page, EventSubscription subscription)
	{
		subscriptions.put(new PageKey(page.getPageId(), Session.get().getId()), subscription);
	}

	public void post(Object event)
	{
		try
		{
			ThreadContext.setApplication(application);
			for (AtmosphereResource resource : broadcaster.getAtmosphereResources())
			{
				PageKey key;
				Collection<EventSubscription> subscriptionsForPage;
				synchronized (this)
				{
					key = trackedPages.get(AtmosphereBehavior.getUUID(resource));
					subscriptionsForPage = Collections2.filter(
						Collections.unmodifiableCollection(subscriptions.get(key)),
						new EventFilter(event));
				}
				post(resource, key, subscriptionsForPage, event);
			}
		}
		finally
		{
			ThreadContext.detach();
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
		PushWebRequest request = new PushWebRequest(application.newWebRequest(httpRequest,
			filterPath), pageKey, subscriptionsForPage, event);
		Response response = new StringWebResponse();
		if (application.createRequestCycle(request, response).processRequestAndDetach())
			broadcaster.broadcast(response.toString(), resource);
	}

	@Override
	public synchronized void sessionUnbound(String sessionId)
	{
		Iterator<PageKey> it = Iterators.concat(trackedPages.values().iterator(),
			subscriptions.keySet().iterator());
		while (it.hasNext())
			if (it.next().isForSession(sessionId))
				it.remove();
	}
}
