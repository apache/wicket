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
package org.apache.wicket.resource.aggregation;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.dependencies.AbstractResourceDependentResourceReference.ResourceType;

/**
 * A data holder built for the {@link AbstractResourceAggregatingHeaderResponse} that groups the
 * resource reference with a boolean representing whether it is css (or, if not, it is js), and the
 * string that was passed in to the responsible render*Reference method (for JS, this is ID, for
 * CSS, this is media).
 * 
 * It acts as a temporary data holder while the IHeaderContributors are being called so that at the
 * end of their traversal, we can render these references in an aggregated way, and still have the
 * appropriate data (i.e. was it CSS or JS) to render it properly.
 * 
 * @author Jeremy Thomerson
 */
// TODO Wicket.next - Improve this class by splitting it in more specialized ones (one for JS,
// another for CSS, ...)
public class ResourceReferenceAndStringData
{

	private final ResourceReference reference;
	private final PageParameters parameters;
	private final String url;
	private final String idOrMedia;
	private final ResourceType type;
	private final boolean jsDefer;
	private final String charset;
	private final String cssCondition;
	private final CharSequence content;

	/**
	 * Construct.
	 * 
	 * @param reference
	 * @param parameters
	 * @param url
	 * @param idOrMedia
	 * @param type
	 * @param jsDefer
	 * @param charset
	 * @param cssCondition
	 */
	public ResourceReferenceAndStringData(ResourceReference reference, PageParameters parameters,
		String url, String idOrMedia, ResourceType type, boolean jsDefer, String charset,
		String cssCondition)
	{
		this.reference = reference;
		this.parameters = parameters;
		this.url = url;
		this.idOrMedia = idOrMedia;
		this.type = type;
		this.jsDefer = jsDefer;
		this.charset = charset;
		this.cssCondition = cssCondition;
		content = null;
	}

	/**
	 * Construct.
	 * 
	 * @param reference
	 * @param idOrMedia
	 * @param isCss
	 * @deprecated use the other constructors instead
	 */
	@Deprecated
	public ResourceReferenceAndStringData(ResourceReference reference, String idOrMedia,
		boolean isCss)
	{
		this.reference = reference;
		parameters = null;
		url = null;
		this.idOrMedia = idOrMedia;
		type = isCss ? ResourceType.CSS : ResourceType.JS;
		jsDefer = false;
		charset = null;
		cssCondition = null;
		content = null;
	}

	/**
	 * Construct.
	 * 
	 * @param content
	 * @param type
	 * @param idOrMedia
	 */
	public ResourceReferenceAndStringData(CharSequence content, ResourceType type, String idOrMedia)
	{
		this.content = content;
		this.type = type;
		reference = null;
		parameters = null;
		url = null;
		this.idOrMedia = idOrMedia;
		jsDefer = false;
		charset = null;
		cssCondition = null;
	}

	/**
	 * @return the resource reference that the user rendered
	 */
	public ResourceReference getReference()
	{
		return reference;
	}

	/**
	 * @return the parameters for the resource reference
	 */
	public PageParameters getParameters()
	{
		return parameters;
	}

	/**
	 * @return the resource reference that the user rendered
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @return the string representing media (if this isCss()), or id (if not, meaning it's js)
	 */
	public String getIdOrMedia()
	{
		return idOrMedia;
	}

	/**
	 * @return the string representing media (if this isCss()), or id (if not, meaning it's js)
	 * @deprecated use {@link #getIdOrMedia()} instead
	 */
	@Deprecated
	public String getString()
	{
		return getIdOrMedia();
	}

	/**
	 * @return true if this is css, false if it's js
	 * @deprecated Use {@link #getResourceType()} instead.
	 */
	@Deprecated
	public boolean isCss()
	{
		return type == ResourceType.CSS;
	}

	/**
	 * @return the type of the resource
	 */
	public ResourceType getResourceType()
	{
		return type;
	}

	/**
	 * @return whether the script should be deferred
	 */
	public boolean isJsDefer()
	{
		return jsDefer;
	}

	/**
	 * @return the charset to use when loading the script
	 */
	public String getCharset()
	{
		return charset;
	}

	/**
	 * @return the IE CSS condition
	 */
	public String getCssCondition()
	{
		return cssCondition;
	}

	/**
	 * @return inline content of CSS or JS contribution
	 */
	public CharSequence getContent()
	{
		return content;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((charset == null) ? 0 : charset.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((cssCondition == null) ? 0 : cssCondition.hashCode());
		result = prime * result + ((idOrMedia == null) ? 0 : idOrMedia.hashCode());
		result = prime * result + (jsDefer ? 1231 : 1237);
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceReferenceAndStringData other = (ResourceReferenceAndStringData)obj;
		if (charset == null)
		{
			if (other.charset != null)
				return false;
		}
		else if (!charset.equals(other.charset))
			return false;
		if (content == null)
		{
			if (other.content != null)
				return false;
		}
		else if (!content.equals(other.content))
			return false;
		if (cssCondition == null)
		{
			if (other.cssCondition != null)
				return false;
		}
		else if (!cssCondition.equals(other.cssCondition))
			return false;
		if (idOrMedia == null)
		{
			if (other.idOrMedia != null)
				return false;
		}
		else if (!idOrMedia.equals(other.idOrMedia))
			return false;
		if (jsDefer != other.jsDefer)
			return false;
		if (parameters == null)
		{
			if (other.parameters != null)
				return false;
		}
		else if (!parameters.equals(other.parameters))
			return false;
		if (reference == null)
		{
			if (other.reference != null)
				return false;
		}
		else if (!reference.equals(other.reference))
			return false;
		if (type != other.type)
			return false;
		if (url == null)
		{
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ResourceReferenceAndStringData [reference=" + reference + ", parameters=" +
			parameters + ", url=" + url + ", idOrMedia=" + idOrMedia + ", type=" + type +
			", jsDefer=" + jsDefer + ", charset=" + charset + ", cssCondition=" + cssCondition +
			", content=" + content + "]";
	}
}