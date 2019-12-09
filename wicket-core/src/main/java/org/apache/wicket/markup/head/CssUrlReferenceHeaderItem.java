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
public class CssUrlReferenceHeaderItem extends CssHeaderItem
{
	private static final long serialVersionUID = 1L;

	private final String url;
	private final String media;
	private final String rel;

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
		this.url = url;
		this.media = media;
		this.rel = rel;
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
		this.url = url;
		this.media = media;
		this.rel = null;
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

	/**
	 * @return the rel attribute content
	 */
	public String getRel()
	{
		return rel;
	}

	@Override
	public void render(Response response)
	{
		internalRenderCSSReference(response,
			UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()), getMedia(), getRel());
	}

	@Override
	public Iterable<?> getRenderTokens()
	{
		return Arrays.asList(
			"css-" + UrlUtils.rewriteToContextRelative(getUrl(), RequestCycle.get()) + "-" + media);
	}

	@Override
	public String toString()
	{
		return "CSSUrlReferenceHeaderItem(" + getUrl() + ")";
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(super.hashCode(), url, media, rel);
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
		return Objects.equals(url, that.url) && Objects.equals(media, that.media) &&
			Objects.equals(rel, that.rel);
	}
}
