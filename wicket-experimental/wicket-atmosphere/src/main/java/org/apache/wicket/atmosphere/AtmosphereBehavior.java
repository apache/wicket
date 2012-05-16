package org.apache.wicket.atmosphere;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.MetaDataKey;
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

	private Component component;

	/**
	 * Construct.
	 */
	public AtmosphereBehavior()
	{
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
	public void onResourceRequested()
	{
		RequestCycle requestCycle = RequestCycle.get();
		ServletWebRequest request = (ServletWebRequest)requestCycle.getRequest();

		// Grab a Meteor
		Meteor meteor = Meteor.build(request.getContainerRequest());
		String uuid = getUUID(meteor.getAtmosphereResource());
		component.getPage().setMetaData(ATMOSPHERE_UUID, uuid);
		EventBus.get().registerPage(uuid, component.getPage());

		// Add us to the listener list.
		meteor.addListener(this);

		String transport = request.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
		if (HeaderConfig.LONG_POLLING_TRANSPORT.equalsIgnoreCase(transport))
		{
			// request.getContainerRequest().setAttribute(ApplicationConfig.RESUME_ON_BROADCAST,
			// Boolean.TRUE);
			meteor.suspend(-1, false);
		}
		else
		{
			meteor.suspend(-1);
		}
	}

	@Override
	public void onBroadcast(AtmosphereResourceEvent event)
	{
		log.info("onBroadcast: {}", event.getMessage());

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
		if (log.isInfoEnabled())
		{
			String transport = event.getResource().getRequest().getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
			HttpServletRequest req = event.getResource().getRequest();
			log.info(String.format("Suspending the %s response from ip %s:%s", transport == null
				? "websocket" : transport, req.getRemoteAddr(), req.getRemotePort()));
		}
	}

	@Override
	public void onResume(AtmosphereResourceEvent event)
	{
		if (log.isInfoEnabled())
		{
			String transport = event.getResource().getRequest().getHeader("X-Atmosphere-Transport");
			HttpServletRequest req = event.getResource().getRequest();
			log.info(String.format("Resuming the %s response from ip %s:%s", transport == null
				? "websocket" : transport, req.getRemoteAddr(), req.getRemotePort()));
		}
	}

	@Override
	public void onDisconnect(AtmosphereResourceEvent event)
	{
		if (log.isInfoEnabled())
		{
			String transport = event.getResource().getRequest().getHeader("X-Atmosphere-Transport");
			HttpServletRequest req = event.getResource().getRequest();
			log.info(String.format("%s connection dropped from ip %s:%s", transport == null
				? "websocket" : transport, req.getRemoteAddr(), req.getRemotePort()));
		}
	}

	@Override
	public void onThrowable(AtmosphereResourceEvent event)
	{
		log.error(event.throwable().getMessage(), event.throwable());
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		try
		{
			CoreLibrariesContributor.contributeAjax(component.getApplication(), response);

			response.render(JavaScriptHeaderItem.forReference(JQueryWicketAtmosphereResourceReference.get()));
			JSONObject options = new JSONObject();
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
	 * @param resource
	 * @return the unique id for the given suspended connection
	 */
	public static String getUUID(AtmosphereResource resource)
	{
		String trackingId = resource.getRequest().getHeader(HeaderConfig.X_ATMOSPHERE_TRACKING_ID);
		if (trackingId != null)
			return trackingId;
		return resource.getRequest().getHeader("Sec-WebSocket-Key");
	}
}