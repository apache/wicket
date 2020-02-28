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
import java.util.Objects;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * {@link HeaderItem} for style tags that are rendered using a fixed URL, for example resources from
 * an external site or context relative urls.
 * 
 * @author papegaaij
 */
public class CssUrlReferenceHeaderItem extends AbstractCssReferenceHeaderItem
{
	private static final long serialVersionUID = 1L;

	private final String url;

	/**
	 * Creates a new {@code CSSUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 * @param rel
	 *            the rel attribute content
	 */
	public CssUrlReferenceHeaderItem(String url, String media, String rel)
	{
		super(media, rel);
		
		this.url = url;
	}

	/**
	 * Creates a new {@code CSSUrlReferenceHeaderItem}.
	 * 
	 * @param url
	 *            context-relative url of the CSS resource
	 * @param media
	 *            the media type for this CSS ("print", "screen", etc.)
	 */
	public CssUrlReferenceHeaderItem(String url, String media)
	{
		super(media, null);
		
		this.url = url;
	}

	/**
	 * @return context-relative url of the CSS resource
	 */
	public String getUrl()
	{
		return url;
	}

	@Override
	public void render(Response response)
	{
		internalRenderCSSReference(response, UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()));
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Arrays.asList(
			"css-" + UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()) + "-" + getMedia());
	}

	@Override
	public String toString()
	{
		return "CSSUrlReferenceHeaderItem(" + getUrl() + ")";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), url, getMedia(), getRel());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		CssUrlReferenceHeaderItem that = (CssUrlReferenceHeaderItem)o;
		return Objects.equals(url, that.url) && Objects.equals(getMedia(), that.getMedia()) &&
			Objects.equals(getRel(), that.getRel());
	}
}
