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
package org.apache.wicket.markup.head;

import java.util.Arrays;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * {@link HeaderItem} for style tags that are rendered using a fixed URL, for example resources from
 * an external site or context relative urls.
 * 
 * @author papegaaij
 */
public class CssUrlReferenceHeaderItem extends CssHeaderItem
{
	private final String url;
	private final String media;

	/**
	 * Creates a new {@code CSSUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 * @param condition
	 *            the condition to use for Internet Explorer conditional comments. E.g. "IE 7".
	 */
	public CssUrlReferenceHeaderItem(String url, String media, String condition)
	{
		super(condition);
		this.url = url;
		this.media = media;
	}

	/**
	 * @return context-relative url of the CSS resource
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @return the media type for this CSS ("print", "screen", etc.)
	 */
	public String getMedia()
	{
		return media;
	}

	@Override
	public void render(Response response)
	{
		internalRenderCSSReference(response,
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()), getMedia(),
			getCondition());
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Arrays.asList("css-" +
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()) + "-" + media);
	}

	@Override
	public String toString()
	{
		return "CSSUrlReferenceHeaderItem(" + getUrl() + ")";
	}

	@Override
	public int hashCode()
	{
		return getUrl().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof CssUrlReferenceHeaderItem)
			return ((CssUrlReferenceHeaderItem)obj).getUrl().equals(getUrl());
		return false;
	}
}
