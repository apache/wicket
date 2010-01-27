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
package org.apache.wicket.ng.request.mapper;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.Url.QueryParameter;
import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.mapper.info.PageComponentInfo;
import org.apache.wicket.ng.request.mapper.parameters.IPageParametersEncoder;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;

/**
 * Convenience class for implementing page/components related encoders.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractMapper implements IRequestMapper
{
	/**
	 * Construct.
	 */
	public AbstractMapper()
	{
	}

	protected IMapperContext getContext()
	{
		return Application.get().getEncoderContext();
	}

	/**
	 * Converts the specified listener interface to String.
	 * 
	 * @param listenerInterface
	 * @return listenerInterface name as string
	 */
	protected String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
	{
		Checks.argumentNotNull(listenerInterface, "listenerInterface");

		return getContext().requestListenerInterfaceToString(listenerInterface);
	}

	/**
	 * Creates listener interface from the specified string
	 * 
	 * @param interfaceName
	 * @return listener interface
	 */
	protected RequestListenerInterface requestListenerInterfaceFromString(String interfaceName)
	{
		Checks.argumentNotEmpty(interfaceName, "interfaceName");

		return getContext().requestListenerInterfaceFromString(interfaceName);
	}

	/**
	 * If the string is in a placeholder format ${key} this method returns the key.
	 * 
	 * @param s
	 * @return placeholder key or <code>null</code> if string is not in right format
	 */
	protected static String getPlaceholder(String s)
	{
		if (s == null || s.length() < 4 || !s.startsWith("${") || !s.endsWith("}"))
		{
			return null;
		}
		else
		{
			return s.substring(2, s.length() - 1);
		}
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
	/*
	 * TODO its funny to have a dependency in this method on the #getPlaceHolder method because we
	 * are forcing everyone who extends this to use the ${placeholder} syntax. We should move
	 * #getPlaceHolder out and rewrite this method to have no dependency on it and match only exact
	 * paths. The mappers that use the syntax will have to provide their own implementations.
	 * 
	 * Either that or have a pluggable placeholder syntax Placeholder { string start, string end}
	 */
	protected boolean urlStartsWith(Url url, String... segments)
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
					if (segments[i].equals(url.getSegments().get(i)) == false &&
						getPlaceholder(segments[i]) == null)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Extracts the {@link PageComponentInfo} from the URL. The {@link PageComponentInfo} is encoded
	 * as the very first query parameter and the parameter consists of name only (no value).
	 * 
	 * @param url
	 * 
	 * @return PageComponentInfo instance if one was encoded in URL, <code>null</code> otherwise.
	 */
	protected PageComponentInfo getPageComponentInfo(Url url)
	{
		if (url == null)
		{
			throw new IllegalStateException("Argument 'url' may not be null.");
		}
		if (url.getQueryParameters().size() > 0)
		{
			QueryParameter param = url.getQueryParameters().get(0);
			if (Strings.isEmpty(param.getValue()))
			{
				return PageComponentInfo.parse(param.getName());
			}
		}
		return null;
	}

	/**
	 * Encodes the {@link PageComponentInfo} instance as the first query string parameter to the
	 * URL.
	 * 
	 * @param url
	 * @param info
	 */
	protected void encodePageComponentInfo(Url url, PageComponentInfo info)
	{
		if (url == null)
		{
			throw new IllegalStateException("Argument 'url' may not be null.");
		}
		if (info != null)
		{
			String s = info.toString();
			if (!Strings.isEmpty(s))
			{
				QueryParameter parameter = new QueryParameter(s, "");
				url.getQueryParameters().add(parameter);
			}
		}
	}

	/**
	 * Loads page class with given name.
	 * 
	 * @param name
	 * @return class
	 */
	protected Class<? extends IRequestablePage> getPageClass(String name)
	{
		Checks.argumentNotEmpty(name, "name");

		return Classes.resolveClass(name);
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
	protected PageParameters extractPageParameters(Request request, int segmentsToSkip,
		IPageParametersEncoder encoder)
	{
		Checks.argumentNotNull(request, "request");
		Checks.argumentNotNull(encoder, "encoder");

		// strip the segments and first query parameter from URL
		Url urlCopy = new Url(request.getUrl());
		while (segmentsToSkip > 0 && urlCopy.getSegments().isEmpty() == false)
		{
			urlCopy.getSegments().remove(0);
			--segmentsToSkip;
		}

		if (!urlCopy.getQueryParameters().isEmpty() &&
			Strings.isEmpty(urlCopy.getQueryParameters().get(0).getValue()))
		{
			urlCopy.getQueryParameters().remove(0);
		}

		PageParameters decoded = encoder.decodePageParameters(request.requestWithUrl(urlCopy));
		return decoded != null ? decoded : new PageParameters();
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
		IPageParametersEncoder encoder)
	{
		Checks.argumentNotNull(url, "url");
		Checks.argumentNotNull(encoder, "encoder");

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
		if (mountPath.startsWith("/"))
		{
			mountPath = mountPath.substring(1);
		}
		Url url = Url.parse(mountPath);

		if (url.getSegments().isEmpty())
		{
			throw new IllegalArgumentException("Mount path must have at least one segment.");
		}

		String[] res = new String[url.getSegments().size()];
		for (int i = 0; i < res.length; ++i)
		{
			res[i] = url.getSegments().get(i);
		}
		return res;
	}
}
