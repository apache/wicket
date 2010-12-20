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

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.response.NullResponse;
import org.apache.wicket.util.string.JavaScriptUtils;
import org.apache.wicket.util.string.Strings;


/**
 * Default implementation of the {@link IHeaderResponse} interface.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class HeaderResponse implements IHeaderResponse
{
	private final Set<Object> rendered = new HashSet<Object>();

	private boolean closed;

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#markRendered(java.lang.Object)
	 */
	public final void markRendered(Object object)
	{
		rendered.add(object);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.request.resource.ResourceReference)
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
			CharSequence url = RequestCycle.get().urlFor(handler);
			internalRenderCSSReference(url.toString(), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.request.resource.ResourceReference,
	 *      String)
	 */
	public void renderCSSReference(ResourceReference reference, String media)
	{
		renderCSSReference(reference, null, media);
	}

	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference, pageParameters);
			CharSequence url = RequestCycle.get().urlFor(handler);
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
			List<String> token = Arrays.asList("css", url, media);
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
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavaScriptReference(org.apache.wicket.request.resource.ResourceReference)
	 */
	public void renderJavaScriptReference(ResourceReference reference)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference);
			CharSequence url = RequestCycle.get().urlFor(handler);
			internalRenderJavaScriptReference(url.toString(), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavaScriptReference(org.apache.wicket.request.resource.ResourceReference,
	 *      String)
	 */
	public void renderJavaScriptReference(ResourceReference reference, String id)
	{
		renderJavaScriptReference(reference, null, id);
	}

	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("reference cannot be null");
		}
		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference, pageParameters);
			CharSequence url = RequestCycle.get().urlFor(handler);
			internalRenderJavaScriptReference(url.toString(), id);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavaScriptReference(java.lang.String)
	 */
	public void renderJavaScriptReference(String url)
	{
		internalRenderJavaScriptReference(relative(url), null);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavaScriptReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderJavaScriptReference(String url, String id)
	{
		internalRenderJavaScriptReference(relative(url), id);
	}

	private void internalRenderJavaScriptReference(String url, String id)
	{
		if (Strings.isEmpty(url))
		{
			throw new IllegalArgumentException("url cannot be empty or null");
		}
		if (!closed)
		{
			List<String> token1 = Arrays.asList("javascript", url);
			List<String> token2 = (id != null) ? Arrays.asList("javascript", id) : null;

			final boolean token1Unused = wasRendered(token1) == false;
			final boolean token2Unused = (token2 != null) ? wasRendered(token2) == false : true;

			if (token1Unused && token2Unused)
			{
				JavaScriptUtils.writeJavaScriptUrl(getResponse(), url, id);
				markRendered(token1);
				if (token2 != null)
				{
					markRendered(token2);
				}
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavaScript(java.lang.CharSequence,
	 *      java.lang.String)
	 */
	public void renderJavaScript(CharSequence javascript, String id)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			List<String> token = Arrays.asList(javascript.toString(), id);
			if (wasRendered(token) == false)
			{
				JavaScriptUtils.writeJavaScript(getResponse(), javascript, id);
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
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnDomReadyJavaScript(java.lang.String)
	 */
	public void renderOnDomReadyJavaScript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			renderOnEventJavaScript("window", "domready", javascript);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnLoadJavaScript(java.lang.String)
	 */
	public void renderOnLoadJavaScript(String javascript)
	{
		if (javascript == null)
		{
			throw new IllegalArgumentException("javascript cannot be null");
		}
		if (!closed)
		{
			renderOnEventJavaScript("window", "load", javascript);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnEventJavaScript(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void renderOnEventJavaScript(String target, String event, String javascript)
	{
		if (!closed)
		{
			List<String> token = Arrays.asList("javascript-event", target, event, javascript);
			if (wasRendered(token) == false)
			{
				renderJavaScriptReference(WicketEventReference.INSTANCE);
				JavaScriptUtils.writeJavaScript(getResponse(), "Wicket.Event.add(" + target +
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
	private String relative(final String location)
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
