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
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.response.NullResponse;
import org.apache.wicket.util.string.JavascriptUtils;


/**
 * Default implementation of the {@link IHeaderResponse} interface.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class HeaderResponse implements IHeaderResponse
{
	private static final long serialVersionUID = 1L;

	private final Set rendered = new HashSet();

	private boolean closed;

	/**
	 * Creates a new header response instance.
	 */
	public HeaderResponse()
	{
		if (Application.exists())
		{
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
		if (!closed)
		{
			CharSequence url = RequestCycle.get().urlFor(reference);
			renderCSSReference(url.toString(), null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.ResourceReference,
	 *      java.lang.String)
	 */
	public void renderCSSReference(ResourceReference reference, String media)
	{
		if (!closed)
		{
			CharSequence url = RequestCycle.get().urlFor(reference);
			renderCSSReference(url.toString(), media);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String)
	 */
	public void renderCSSReference(String url)
	{
		if (!closed)
		{
			renderCSSReference(url, null);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderCSSReference(String url, String media)
	{
		if (!closed)
		{
			List token = Arrays.asList(new Object[] { "css", url, media });
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
				getResponse().println(" />");
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(org.apache.wicket.ResourceReference)
	 */
	public void renderJavascriptReference(ResourceReference reference)
	{
		if (!closed)
		{
			CharSequence url = RequestCycle.get().urlFor(reference);
			renderJavascriptReference(url.toString());
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(org.apache.wicket.ResourceReference,
	 *      java.lang.String)
	 */
	public void renderJavascriptReference(ResourceReference reference, String id)
	{
		if (!closed)
		{
			CharSequence url = RequestCycle.get().urlFor(reference);
			renderJavascriptReference(url.toString(), id);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(java.lang.String)
	 */
	public void renderJavascriptReference(String url)
	{
		if (!closed)
		{
			List token = Arrays.asList(new Object[] { "javascript", url });
			if (wasRendered(token) == false)
			{
				JavascriptUtils.writeJavascriptUrl(getResponse(), url);
				markRendered(token);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderJavascriptReference(String url, String id)
	{
		if (!closed)
		{
			List token1 = Arrays.asList(new Object[] { "javascript", url });
			List token2 = Arrays.asList(new Object[] { "javascript", id });
			if (wasRendered(token1) == false && wasRendered(token2))
			{
				JavascriptUtils.writeJavascriptUrl(getResponse(), url, id);
				markRendered(token1);
				markRendered(token2);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascript(java.lang.CharSequence,
	 *      java.lang.String)
	 */
	public void renderJavascript(CharSequence javascript, String id)
	{
		if (!closed)
		{
			List token = Arrays.asList(new Object[] { javascript.toString(), id });
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
		if (!closed)
		{
			renderOnEventJavacript("window", "domready", javascript);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnLoadJavascript(java.lang.String)
	 */
	public void renderOnLoadJavascript(String javascript)
	{
		if (!closed)
		{
			renderOnEventJavacript("window", "load", javascript);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnEventJavacript(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void renderOnEventJavacript(String target, String event, String javascript)
	{
		if (!closed)
		{
			List token = Arrays.asList(new Object[] { "javascript-event", target, event, javascript });
			if (wasRendered(token) == false)
			{
				renderJavascriptReference(WicketEventReference.INSTANCE);
				JavascriptUtils.writeJavascript(getResponse(), "Wicket.Event.add(" + target + ", \"" +
						event + "\", function() { " + javascript + ";});");
				markRendered(token);
			}
		}
	}
	
	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#close()
	 */
	public void close()
	{
		this.closed = true;
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
	 * Once the HeaderResponse is closed, no output may be written to it anymore. To enforce that,
	 * the {@link #getResponse()} is defined final in this class and will return a NullResponse instance once closed or otherwise
	 * the Response provided by this method.
	 * @return Response
	 */
	protected abstract Response getRealResponse();
}
