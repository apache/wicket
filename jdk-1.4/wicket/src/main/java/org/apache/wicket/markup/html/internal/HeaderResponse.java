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

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WicketEventReference;
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

	/**
	 * Creates a new header response instance.
	 * 
	 * @param response
	 *            response used to write the head elements
	 */
	public HeaderResponse()
	{
		
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#markRendered(java.lang.Object)
	 */
	public final void markRendered(Object object)
	{
		rendered.add(object);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.markup.html.ResourceReference)
	 */
	public void renderCSSReference(ResourceReference reference)
	{
		CharSequence url = RequestCycle.get().urlFor(reference);
		renderCSSReference(url.toString(), null);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(org.apache.wicket.ResourceReference,
	 *      java.lang.String)
	 */
	public void renderCSSReference(ResourceReference reference, String media)
	{
		CharSequence url = RequestCycle.get().urlFor(reference);
		renderCSSReference(url.toString(), media);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String)
	 */
	public void renderCSSReference(String url)
	{
		renderCSSReference(url, null);
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderCSSReference(java.lang.String,
	 *      java.lang.String)
	 */
	public void renderCSSReference(String url, String media)
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

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(org.apache.wicket.markup.html.ResourceReference)
	 */
	public void renderJavascriptReference(ResourceReference reference)
	{
		CharSequence url = RequestCycle.get().urlFor(reference);
		renderJavascriptReference(url.toString());
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascriptReference(java.lang.String)
	 */
	public void renderJavascriptReference(String url)
	{
		List token = Arrays.asList(new Object[] { "javascript", url });
		if (wasRendered(token) == false)
		{
			JavascriptUtils.writeJavascriptUrl(getResponse(), url);
			markRendered(token);
		}
	}


	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderJavascript(java.lang.CharSequence,
	 *      java.lang.String)
	 */
	public void renderJavascript(CharSequence javascript, String id)
	{
		List token = Arrays.asList(new Object[] { javascript.toString(), id });
		if (wasRendered(token) == false)
		{
			JavascriptUtils.writeJavascript(getResponse(), javascript, id);
			markRendered(token);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderString(java.lang.CharSequence)
	 */
	public void renderString(CharSequence string)
	{
		String token = string.toString();
		if (wasRendered(token) == false)
		{
			getResponse().write(string);
			markRendered(token);
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
		List token = Arrays.asList(new Object[] { "javascript-event", "domready", javascript });
		if (wasRendered(token) == false)
		{
			renderJavascriptReference(WicketEventReference.INSTANCE);
			JavascriptUtils.writeJavascript(getResponse(),
					"Wicket.Event.add(window, \"domready\", function() { " + javascript + ";});");
			markRendered(token);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.IHeaderResponse#renderOnLoadJavascript(java.lang.String)
	 */
	public void renderOnLoadJavascript(String javascript)
	{
		List token = Arrays.asList(new Object[] { "javascript-event", "load", javascript });
		if (wasRendered(token) == false)
		{
			renderJavascriptReference(WicketEventReference.INSTANCE);
			JavascriptUtils.writeJavascript(getResponse(),
					"Wicket.Event.add(window, \"load\", function() { " + javascript + ";});");
			markRendered(token);
		}
	}

}
