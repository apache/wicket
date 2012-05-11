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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.response.NullResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.CssUtils;
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

	public void renderCSS(CharSequence css, String id)
	{
		Args.notNull(css, "css");

		if (!closed)
		{
			List<String> token = Arrays.asList(css.toString(), id);
			if (wasRendered(token) == false)
			{
				renderString(CssUtils.INLINE_OPEN_TAG + css + CssUtils.INLINE_CLOSE_TAG);
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.request.resource.ResourceReference)
	 */
	public void renderCSSReference(ResourceReference reference)
	{
		renderCSSReference(reference, null, null);
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
		renderCSSReference(reference, pageParameters, media, null);
	}

	public void renderCSSReference(ResourceReference reference, PageParameters pageParameters,
		String media, String condition)
	{
		Args.notNull(reference, "reference");

		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference, pageParameters);
			CharSequence url = RequestCycle.get().urlFor(handler);
			internalRenderCSSReference(url.toString(), media, condition);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String)
	 */
	public void renderCSSReference(String url)
	{
		renderCSSReference(url, null);
	}

	public void renderCSSReference(String url, String media)
	{
		renderCSSReference(url, media, null);
	}

	public void renderCSSReference(String url, String media, String condition)
	{
		internalRenderCSSReference(relative(url), media, condition);
	}

	private void internalRenderCSSReference(final String url, final String media,
		final String condition)
	{
		Args.notEmpty(url, "url");
		
		if (!closed)
		{
			String urlWoSessionId = Strings.stripJSessionId(url);
			List<String> token = Arrays.asList("css", urlWoSessionId, media);
			if (wasRendered(token) == false)
			{
				if (Strings.isEmpty(condition) == false)
				{
					getResponse().write("<!--[if ");
					getResponse().write(condition);
					getResponse().write("]>");
				}
				getResponse().write("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
				getResponse().write(Strings.escapeMarkup(urlWoSessionId));
				getResponse().write("\"");
				if (media != null)
				{
					getResponse().write(" media=\"");
					getResponse().write(Strings.escapeMarkup(media));
					getResponse().write("\"");
				}
				getResponse().write(" />");
				if (Strings.isEmpty(condition) == false)
				{
					getResponse().write("<![endif]-->");
				}
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
		renderJavaScriptReference(reference, null);
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
		renderJavaScriptReference(reference, pageParameters, id, false);
	}

	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer)
	{
		renderJavaScriptReference(reference, pageParameters, id, defer, null);
	}

	public void renderJavaScriptReference(ResourceReference reference,
		PageParameters pageParameters, String id, boolean defer, String charset)
	{
		Args.notNull(reference, "reference");

		if (!closed)
		{
			IRequestHandler handler = new ResourceReferenceRequestHandler(reference, pageParameters);
			CharSequence url = RequestCycle.get().urlFor(handler);
			internalRenderJavaScriptReference(url.toString(), id, defer, charset);
		}
	}

	public void renderJavaScriptReference(String url)
	{
		renderJavaScriptReference(url, null);
	}

	public void renderJavaScriptReference(String url, String id)
	{
		renderJavaScriptReference(url, id, false);
	}

	public void renderJavaScriptReference(String url, String id, boolean defer)
	{
		renderJavaScriptReference(url, id, defer, null);
	}

	public void renderJavaScriptReference(String url, String id, boolean defer, String charset)
	{
		internalRenderJavaScriptReference(relative(url), id, defer, charset);
	}

	private void internalRenderJavaScriptReference(String url, String id, boolean defer,
		String charset)
	{
		Args.notEmpty(url, "url");

		if (!closed)
		{
			String urlWoSessionId = Strings.stripJSessionId(url);

			List<String> token1 = Arrays.asList("javascript", urlWoSessionId);
			List<String> token2 = (id != null) ? Arrays.asList("javascript", id) : null;

			final boolean token1Unused = wasRendered(token1) == false;
			final boolean token2Unused = (token2 == null) || wasRendered(token2) == false;

			if (token1Unused && token2Unused)
			{
				JavaScriptUtils.writeJavaScriptUrl(getResponse(), urlWoSessionId, id, defer,
					charset);
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
		Args.notNull(javascript, "javascript");

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
		Args.notNull(string, "string");

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
	 * Rewrites a relative url into a context-relative one, leaves absolute urls alone
	 * 
	 * @param url
	 * @return relative path
	 */
	private String relative(final String url)
	{
		Args.notEmpty(url, "location");

		if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))
		{
			return url;
		}

		RequestCycle rc = RequestCycle.get();
		return rc.getUrlRenderer().renderContextRelativeUrl(url);
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
