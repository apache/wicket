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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.cpr.Meteor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code AtmosphereBehavior} manages the suspended connection from the client. It adds the required
 * javascript libraries to the markup which setup a suspended connection. This connection can be
 * websocket, streaming http or long-polling, depending on what the client and server support. This
 * behavior is added automatically to pages with components with event subscriptions.
 * 
 * @author papegaaij
 */
public class AtmosphereBehavior extends Behavior
	implements
		IResourceListener,
		AtmosphereResourceEventListener
{
	private static final Logger log = LoggerFactory.getLogger(AtmosphereBehavior.class);

	/**
	 * The key under which a unique id is stored in the page. This id is unique for all clients.
	 */
	public static final MetaDataKey<String> ATMOSPHERE_UUID = new MetaDataKey<String>()
	{
		private static final long serialVersionUID = 1L;
	};

	private static final long serialVersionUID = 1L;

	private String applicationKey;

	private Component component;


	/**
	 * Construct.
	 */
	public AtmosphereBehavior()
	{
		applicationKey = Application.get().getApplicationKey();
	}

	private EventBus findEventBus()
	{
		return EventBus.get(Application.get(applicationKey));
	}

	@Override
	public void bind(Component component)
	{
		this.component = component;
	}

	@Override
	public final boolean getStatelessHint(Component component)
	{
		return false;
	}

	@Override
	public void onPreSuspend(AtmosphereResourceEvent event)
	{
	}

	@Override
	public void onClose(AtmosphereResourceEvent event)
	{
	}

	@Override
	public void onResourceRequested()
	{
		RequestCycle requestCycle = RequestCycle.get();
		ServletWebRequest request = (ServletWebRequest)requestCycle.getRequest();

		// Grab a Meteor
		Meteor meteor = Meteor.build(request.getContainerRequest());
		// Add us to the listener list.
		meteor.addListener(this);
		meteor.suspend(-1);

		String uuid = meteor.getAtmosphereResource().uuid();
		Page page = component.getPage();
		page.setMetaData(ATMOSPHERE_UUID, uuid);
		findEventBus().registerPage(uuid, page);
	}

	@Override
	public void onBroadcast(AtmosphereResourceEvent event)
	{
		if (log.isDebugEnabled())
		{
			log.debug("onBroadcast: {}", event.getMessage());
		}

		// If we are using long-polling, resume the connection as soon as we get
		// an event.
		String transport = event.getResource()
			.getRequest()
			.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);

		if (HeaderConfig.LONG_POLLING_TRANSPORT.equalsIgnoreCase(transport))
		{
			Meteor meteor = Meteor.lookup(event.getResource().getRequest());
			meteor.resume();
		}
	}

	@Override
	public void onSuspend(AtmosphereResourceEvent event)
	{
		if (log.isDebugEnabled())
		{
			AtmosphereRequest atmosphereRequest = event.getResource().getRequest();
			String transport = atmosphereRequest.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
			log.debug(String.format("Suspending the %s response from ip %s:%s", transport == null
				? "websocket" : transport, atmosphereRequest.getRemoteAddr(), atmosphereRequest.getRemotePort()));
		}
	}

	@Override
	public void onResume(AtmosphereResourceEvent event)
	{
		if (log.isDebugEnabled())
		{
			AtmosphereRequest atmosphereRequest = event.getResource().getRequest();
			String transport = atmosphereRequest.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
			log.debug(String.format("Resuming the %s response from ip %s:%s", transport == null
				? "websocket" : transport, atmosphereRequest.getRemoteAddr(), atmosphereRequest.getRemotePort()));
		}
	}

	@Override
	public void onDisconnect(AtmosphereResourceEvent event)
	{
		if (log.isDebugEnabled())
		{
			AtmosphereRequest atmosphereRequest = event.getResource().getRequest();
			String transport = atmosphereRequest.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
			log.debug(String.format("%s connection dropped from ip %s:%s", transport == null
				? "websocket" : transport, atmosphereRequest.getRemoteAddr(), atmosphereRequest.getRemotePort()));
		}
		// It is possible that the application has already been destroyed, in which case
		// unregistration is no longer needed
		if (Application.get(applicationKey) != null)
		{
			findEventBus().unregisterConnection(event.getResource().uuid());
		}
	}

	@Override
	public void onThrowable(AtmosphereResourceEvent event)
	{
		Throwable throwable = event.throwable();
		log.error(throwable.getMessage(), throwable);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		try
		{
			CoreLibrariesContributor.contributeAjax(component.getApplication(), response);

			response.render(JavaScriptHeaderItem.forReference(JQueryWicketAtmosphereResourceReference.get()));
			JSONObject options = findEventBus().getParameters().toJSON();
			options.put("url",
				component.urlFor(this, IResourceListener.INTERFACE, new PageParameters())
					.toString());
			response.render(OnDomReadyHeaderItem.forScript("$('#" + component.getMarkupId() +
				"').wicketAtmosphere(" + options.toString() + ")"));
		}
		catch (JSONException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Find the Atmosphere UUID for the suspended connection for the given page (if any).
	 * 
	 * @param page
	 * @return The UUID of the Atmosphere Resource, or null if no resource is suspended for the
	 *         page.
	 */
	public static String getUUID(Page page)
	{
		return page.getMetaData(ATMOSPHERE_UUID);
	}

	/**
	 * @param resource
	 * @return the unique id for the given suspended connection
	 * @deprecated use {@link AtmosphereResource#uuid()}
	 */
	@Deprecated
	public static String getUUID(AtmosphereResource resource)
	{
		return resource.uuid();
	}
}
