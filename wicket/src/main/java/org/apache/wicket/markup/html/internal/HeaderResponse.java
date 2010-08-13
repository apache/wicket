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
package org.apache.wicket.markup.html.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.response.NullResponse;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.util.string.Strings;


/**
 * Default implementation of the {@link IHeaderResponse} interface.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class HeaderResponse implements IHeaderResponse
{
	private static final long serialVersionUID = 1L;

	private final Set<Object> rendered = new HashSet<Object>();

	private boolean closed;

	/**
	 * Creates a new header response instance.
	 */
	public HeaderResponse()
	{
		if (Application.exists())
		{
			// TODO remove in 1.5; see IHeaderRenderStrategy
			Application.get().notifyRenderHeadListener(this);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#markRendered(java.lang.Object)
	 */
	public final void markRendered(Object object)
	{
		rendered.add(object);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.ResourceReference)
	 */
	public void renderCSSReference(ResourceReference reference)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference);
			CharSequence url = RequestCycle.get().renderUrlFor(handler);
			internalRenderCSSReference(url.toString(), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.ResourceReference,
	 *      java.lang.String)
	 */
	public void renderCSSReference(ResourceReference reference, String media)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference);
			CharSequence url = RequestCycle.get().renderUrlFor(handler);
			internalRenderCSSReference(url.toString(), media);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String)
	 */
	public void renderCSSReference(String url)
	{
		if (Strings.isEmpty(url))
		{
			throw new IllegalArgumentException("url cannot be empty or null");
		}
		if (!closed)
		{
			internalRenderCSSReference(relative(url), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderCSSReference(String url, String media)
	{
		internalRenderCSSReference(relative(url), media);
	}

	private void internalRenderCSSReference(String url, String media)
	{
		if (Strings.isEmpty(url))
		{
			throw new IllegalArgumentException("url cannot be empty or null");
		}
		if (!closed)
		{
			List<Object> token = Arrays.asList(new Object[] { "css", url, media });
			if (wasRendered(token) == false)
			{
				getResponse().write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
				getResponse().write(url);
				getResponse().write("\"");
				if (media != null)
				{
					getResponse().write(" media=\"");
					getResponse().write(media);
					getResponse().write("\"");
				}
				getResponse().write(" />");
				getResponse().write("\n");
				markRendered(token);
			}
		}
	}


	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(org.apache.wicket.ResourceReference)
	 */
	public void renderJavascriptReference(ResourceReference reference)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference);
			CharSequence url = RequestCycle.get().renderUrlFor(handler);
			internalRenderJavascriptReference(url.toString(), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(org.apache.wicket.ResourceReference,
	 *      java.lang.String)
	 */
	public void renderJavascriptReference(ResourceReference reference, String id)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference);
			CharSequence url = RequestCycle.get().renderUrlFor(handler);
			internalRenderJavascriptReference(url.toString(), id);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(java.lang.String)
	 */
	public void renderJavascriptReference(String url)
	{
		internalRenderJavascriptReference(relative(url), null);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderJavascriptReference(String url, String id)
	{
		internalRenderJavascriptReference(relative(url), id);
	}

	private void internalRenderJavascriptReference(String url, String id)
	{
		if (Strings.isEmpty(url))
		{
			throw new IllegalArgumentException("url cannot be empty or null");
		}
		if (!closed)
		{
			List<Object> token1 = Arrays.asList(new Object[] { "javascript", url });
			List<Object> token2 = (id != null) ? Arrays.asList(new Object[] { "javascript", id })
				: null;

			final boolean token1Unused = wasRendered(token1) == false;
			final boolean token2Unused = (token2 != null) ? wasRendered(token2) == false : true;

			if (token1Unused && token2Unused)
			{
				JavascriptUtils.writeJavascriptUrl(getResponse(), url, id);
				markRendered(token1);
				if (token2 != null)
				{
					markRendered(token2);
				}
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascript(java.lang.CharSequence,
	 *      java.lang.String)
	 */
	public void renderJavascript(CharSequence javascript, String id)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			List<Object> token = Arrays.asList(new Object[] { javascript.toString(), id });
			if (wasRendered(token) == false)
			{
				JavascriptUtils.writeJavascript(getResponse(), javascript, id);
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderString(java.lang.CharSequence)
	 */
	public void renderString(CharSequence string)
	{
		if (string == null)
		{
			throw new IllegalArgumentException("string cannot be null");
		}
		if (!closed)
		{
			String token = string.toString();
			if (wasRendered(token) == false)
			{
				getResponse().write(string);
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#wasRendered(java.lang.Object)
	 */
	public final boolean wasRendered(Object object)
	{
		return rendered.contains(object);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnDomReadyJavascript(java.lang.String)
	 */
	public void renderOnDomReadyJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			renderOnEventJavascript("window", "domready", javascript);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnLoadJavascript(java.lang.String)
	 */
	public void renderOnLoadJavascript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			renderOnEventJavascript("window", "load", javascript);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnEventJavascript(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void renderOnEventJavascript(String target, String event, String javascript)
	{
		if (!closed)
		{
			List<Object> token = Arrays.asList(new Object[] { "javascript-event", target, event,
					javascript });
			if (wasRendered(token) == false)
			{
				renderJavascriptReference(WicketEventReference.INSTANCE);
				JavascriptUtils.writeJavascript(getResponse(), "Wicket.Event.add(" + target +
					", \"" + event + "\", function(event) { " + javascript + ";});");
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#close()
	 */
	public void close()
	{
		closed = true;
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#getResponse()
	 */
	public final Response getResponse()
	{
		return closed ? NullResponse.getInstance() : getRealResponse();
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#isClosed()
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * 
	 * @param location
	 * @return relative path
	 */
	private final String relative(final String location)
	{
		if (location.startsWith("http://") || location.startsWith("https://") ||
			location.startsWith("/"))
		{
			return location;
		}

		RequestCycle rc = RequestCycle.get();
		return rc.getUrlRenderer().renderUrl(Url.parse(location, rc.getRequest().getCharset()));
	}

	/**
	 * Once the HeaderResponse is closed, no output may be written to it anymore. To enforce that,
	 * the {@link #getResponse()} is defined final in this class and will return a NullResponse
	 * instance once closed or otherwise the Response provided by this method.
	 * 
	 * @return Response
	 */
	protected abstract Response getRealResponse();
}
