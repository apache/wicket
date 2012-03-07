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
package org.apache.wicket.core.request.mapper;

import java.util.Locale;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Base class for encoding and decoding {@link ResourceReference}s
 *
 * @author Matej Knopp
 */
public abstract class AbstractResourceReferenceMapper extends AbstractComponentMapper
{

	protected final String encodeResourceReferenceAttributes(
		ResourceReference.UrlAttributes attributes)
	{
		if (attributes == null ||
			(attributes.getLocale() == null && attributes.getStyle() == null && attributes.getVariation() == null))
		{
			return null;
		}
		else
		{
			StringBuilder res = new StringBuilder();
			if (attributes.getLocale() != null)
			{
				res.append(attributes.getLocale().toString());
			}
			boolean styleEmpty = Strings.isEmpty(attributes.getStyle());
			if (!styleEmpty)
			{
				res.append('-');
				res.append(attributes.getStyle());
			}
			if (!Strings.isEmpty(attributes.getVariation()))
			{
				if (styleEmpty)
				{
					res.append("--");
				}
				else
				{
					res.append('-');
				}
				res.append(attributes.getVariation());
			}
			return res.toString();
		}
	}

	private static String nonEmpty(String s)
	{
		if (Strings.isEmpty(s))
		{
			return null;
		}
		else
		{
			return s;
		}
	}

	protected final ResourceReference.UrlAttributes decodeResourceReferenceAttributes(
		String attributes)
	{
		Locale locale = null;
		String style = null;
		String variation = null;

		if (!Strings.isEmpty(attributes))
		{
			String split[] = attributes.split("-", 3);
			locale = parseLocale(split[0]);
			if (split.length == 2)
			{
				style = nonEmpty(split[1]);
			}
			else if (split.length == 3)
			{
				style = nonEmpty(split[1]);
				variation = nonEmpty(split[2]);
			}
		}
		return new ResourceReference.UrlAttributes(locale, style, variation);
	}

	private static Locale parseLocale(String locale)
	{
		if (Strings.isEmpty(locale))
		{
			return null;
		}
		else
		{
			String parts[] = locale.toLowerCase().split("_", 3);
			if (parts.length == 1)
			{
				return new Locale(parts[0]);
			}
			else if (parts.length == 2)
			{
				return new Locale(parts[0], parts[1]);
			}
			else if (parts.length == 3)
			{
				return new Locale(parts[0], parts[1], parts[2]);
			}
			else
			{
				return null;
			}
		}
	}

	protected void encodeResourceReferenceAttributes(Url url, ResourceReference reference)
	{
		String encoded = encodeResourceReferenceAttributes(reference.getUrlAttributes());
		if (!Strings.isEmpty(encoded))
		{
			url.getQueryParameters().add(new Url.QueryParameter(encoded, ""));
		}
	}

	protected ResourceReference.UrlAttributes getResourceReferenceAttributes(Url url)
	{
		Args.notNull(url, "url");

		if (url.getQueryParameters().size() > 0)
		{
			QueryParameter param = url.getQueryParameters().get(0);
			if (Strings.isEmpty(param.getValue()))
			{
				return decodeResourceReferenceAttributes(param.getName());
			}
		}
		return new ResourceReference.UrlAttributes(null, null, null);
	}


	/**
	 * {@inheritDoc}
	 *
	 * Remove the first parameter because it brings meta information like locale
	 */
	@Override
	protected void removeMetaParameter(final Url urlCopy)
	{
		urlCopy.getQueryParameters().remove(0);
	}
}
