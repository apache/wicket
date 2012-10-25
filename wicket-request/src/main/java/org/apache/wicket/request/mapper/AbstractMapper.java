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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * 
 */
public abstract class AbstractMapper implements IRequestMapper
{

	/**
	 * If the string is in a placeholder format ${key} this method returns the key.
	 * 
	 * @param s
	 * @return placeholder key or <code>null</code> if string is not in right format
	 */
	protected String getPlaceholder(final String s)
	{
		return getPlaceholder(s, '$');
	}

	/**
	 * If the string is in an optional parameter placeholder format #{key} this method returns the
	 * key.
	 * 
	 * @param s
	 * @return placeholder key or <code>null</code> if string is not in right format
	 */
	protected String getOptionalPlaceholder(final String s)
	{
		return getPlaceholder(s, '#');
	}

	/**
	 * If the string is in a placeholder format x{key}, where 'x' can be specified, this method
	 * returns the key.
	 * 
	 * @param s
	 * @param startChar
	 *            the character used to indicate the start of the placeholder
	 * @return placeholder key or <code>null</code> if string is not in right format
	 */
	protected String getPlaceholder(final String s, char startChar)
	{
		if ((s == null) || (s.length() < 4) || !s.startsWith(startChar + "{") || !s.endsWith("}"))
		{
			return null;
		}
		else
		{
			return s.substring(2, s.length() - 1);
		}
	}

	/**
	 * Construct.
	 */
	public AbstractMapper()
	{
		super();
	}

	/**
	 * Returns true if the given url starts with specified segments. Segments that contain
	 * placelhoders are not compared.
	 * 
	 * @param url
	 * @param segments
	 * @return <code>true</code> if the URL starts with the specified segments, <code>false</code>
	 *         otherwise
	 */
	protected boolean urlStartsWith(final Url url, final String... segments)
	{
		if (url == null)
		{
			return false;
		}
		else
		{
			if (url.getSegments().size() < segments.length)
			{
				return false;
			}
			else
			{
				for (int i = 0; i < segments.length; ++i)
				{
					if ((segments[i].equals(url.getSegments().get(i)) == false) &&
						(getPlaceholder(segments[i]) == null))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Extracts {@link PageParameters} from the URL using the given {@link IPageParametersEncoder} .
	 * 
	 * @param request
	 * @param segmentsToSkip
	 *            how many URL segments should be skipped because they "belong" to the
	 *            {@link IRequestMapper}
	 * @param encoder
	 * @return PageParameters instance
	 */
	protected PageParameters extractPageParameters(final Request request, int segmentsToSkip,
		final IPageParametersEncoder encoder)
	{
		Args.notNull(request, "request");
		Args.notNull(encoder, "encoder");

		// strip the segments and first query parameter from URL
		Url urlCopy = new Url(request.getUrl());
		while ((segmentsToSkip > 0) && (urlCopy.getSegments().isEmpty() == false))
		{
			urlCopy.getSegments().remove(0);
			--segmentsToSkip;
		}

		if (!urlCopy.getQueryParameters().isEmpty() &&
			Strings.isEmpty(urlCopy.getQueryParameters().get(0).getValue()))
		{
			removeMetaParameter(urlCopy);
		}

		return encoder.decodePageParameters(request.cloneWithUrl(urlCopy));
	}

	/**
	 * The new {@link IRequestMapper}s use the first query parameter to hold meta information about
	 * the request like page version, component version, locale, ... The actual
	 * {@link IRequestMapper} implementation can decide whether the this parameter should be removed
	 * before creating {@link PageParameters} from the current {@link Url#getQueryParameters() query
	 * parameters}
	 * 
	 * @param urlCopy
	 *            the {@link Url} that first query parameter has no value
	 */
	protected void removeMetaParameter(final Url urlCopy)
	{
	}

	/**
	 * Encodes the given {@link PageParameters} to the URL using the given
	 * {@link IPageParametersEncoder}. The original URL object is unchanged.
	 * 
	 * @param url
	 * @param pageParameters
	 * @param encoder
	 * @return URL with encoded parameters
	 */
	protected Url encodePageParameters(Url url, PageParameters pageParameters,
		final IPageParametersEncoder encoder)
	{
		Args.notNull(url, "url");
		Args.notNull(encoder, "encoder");

		if (pageParameters == null)
		{
			pageParameters = new PageParameters();
		}

		Url parametersUrl = encoder.encodePageParameters(pageParameters);
		if (parametersUrl != null)
		{
			// copy the url
			url = new Url(url);

			for (String s : parametersUrl.getSegments())
			{
				url.getSegments().add(s);
			}
			for (QueryParameter p : parametersUrl.getQueryParameters())
			{
				url.getQueryParameters().add(p);
			}
		}

		return url;
	}

	/**
	 * Convenience method for representing mountPath as array of segments
	 * 
	 * @param mountPath
	 * @return array of path segments
	 */
	protected String[] getMountSegments(String mountPath)
	{
		if (mountPath.charAt(0) == '/')
		{
			mountPath = mountPath.substring(1);
		}
		Url url = Url.parse(mountPath);

		String[] res = new String[url.getSegments().size()];
		for (int i = 0; i < res.length; ++i)
		{
			res[i] = url.getSegments().get(i);
		}
		return res;
	}

}