package org.apache.wicket.atmosphere;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.time.Time;

public class PushWebRequest extends WebRequest
{
	private WebRequest wrappedRequest;

	private PageKey pageKey;

	private Collection<EventSubscription> subscriptions;

	private Object event;

	public PushWebRequest(WebRequest wrappedRequest, PageKey pageKey,
			Collection<EventSubscription> subscriptions, Object event)
	{
		this.wrappedRequest = wrappedRequest;
		this.pageKey = pageKey;
		this.subscriptions = subscriptions;
		this.event = event;
	}

	public PageKey getPageKey()
	{
		return pageKey;
	}

	public Collection<EventSubscription> getSubscriptions()
	{
		return subscriptions;
	}

	public Object getEvent()
	{
		return event;
	}

	@Override
	public List<Cookie> getCookies()
	{
		return wrappedRequest.getCookies();
	}

	@Override
	public List<String> getHeaders(String name)
	{
		return wrappedRequest.getHeaders(name);
	}

	@Override
	public String getHeader(String name)
	{
		return wrappedRequest.getHeader(name);
	}

	@Override
	public Time getDateHeader(String name)
	{
		return wrappedRequest.getDateHeader(name);
	}

	@Override
	public Url getUrl()
	{
		return wrappedRequest.getUrl();
	}

	@Override
	public Url getClientUrl()
	{
		return wrappedRequest.getClientUrl();
	}

	@Override
	public Locale getLocale()
	{
		return wrappedRequest.getLocale();
	}

	@Override
	public Charset getCharset()
	{
		return wrappedRequest.getCharset();
	}

	@Override
	public Object getContainerRequest()
	{
		return wrappedRequest.getContainerRequest();
	}
}
