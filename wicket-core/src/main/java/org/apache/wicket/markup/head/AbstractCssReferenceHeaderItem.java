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

import java.util.Objects;

import org.apache.wicket.core.util.string.CssUtils;
import org.apache.wicket.markup.html.CrossOrigin;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.value.AttributeMap;

/**
 * A {@link org.apache.wicket.markup.head.HeaderItem} that renders a CSS reference.
 */
public abstract class AbstractCssReferenceHeaderItem extends CssHeaderItem
{
	private final String media;
	private final String rel;
	private CrossOrigin crossOrigin;
	private String integrity;

	public AbstractCssReferenceHeaderItem(String media, String rel)
	{
		this.media = media;
		this.rel = rel;
	}

	public CrossOrigin getCrossOrigin()
	{
		return crossOrigin;
	}

	public AbstractCssReferenceHeaderItem setCrossOrigin(CrossOrigin crossOrigin)
	{
		this.crossOrigin = crossOrigin;
		return this;
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

	public String getIntegrity()
	{
		return integrity;
	}

	public AbstractCssReferenceHeaderItem setIntegrity(String integrity)
	{
		this.integrity = integrity;
		return this;
	}

	protected final void internalRenderCSSReference(Response response, String url)
	{
		Args.notEmpty(url, "url");

		AttributeMap attributes = new AttributeMap();
		attributes.putAttribute(CssUtils.ATTR_LINK_REL, getRel() == null ? "stylesheet" : getRel());
		attributes.putAttribute(CssUtils.ATTR_TYPE, "text/css");
		attributes.putAttribute(CssUtils.ATTR_LINK_HREF, url);
		attributes.putAttribute(CssUtils.ATTR_ID, getId());
		attributes.putAttribute(CssUtils.ATTR_LINK_MEDIA, getMedia());
		attributes.putAttribute(CssUtils.ATTR_CROSS_ORIGIN,
			crossOrigin == null ? null : crossOrigin.getRealName());
		attributes.putAttribute(CssUtils.ATTR_INTEGRITY, integrity);
		attributes.putAttribute(CssUtils.ATTR_CSP_NONCE, getNonce());
		CssUtils.writeLink(response, attributes);

		response.write("\n");
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AbstractCssReferenceHeaderItem that = (AbstractCssReferenceHeaderItem)o;
		return Objects.equals(integrity, that.integrity)
			&& Objects.equals(crossOrigin, that.crossOrigin);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(integrity, crossOrigin);
	}
}
