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
package org.apache._wicket.request.encoder;

import org.apache._wicket.IComponent;
import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandlerEncoder;
import org.apache._wicket.request.RequestParameters;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.Url.QueryParameter;
import org.apache._wicket.request.encoder.info.PageComponentInfo;
import org.apache._wicket.request.encoder.info.PageInfo;
import org.apache._wicket.request.encoder.parameters.PageParametersEncoder;
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;

/**
 * Convenience class for implementing page/components related encoders.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractEncoder implements RequestHandlerEncoder
{
	protected EncoderContext getContext()
	{
		// TODO
		return null;
	};

	/**
	 * Converts the specified listener interface to String.
	 * 
	 * @param listenerInterface
	 * @return listenerInterface name as string
	 */
	protected String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
	{
		if (listenerInterface == null)
		{
			throw new IllegalArgumentException("Argument 'listenerInterface' may not be null.");
		}
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
		if (interfaceName == null)
		{
			throw new IllegalArgumentException("Argument 'interfaceName' may not be null.");
		}
		return getContext().requestListenerInterfaceFromString(interfaceName);
	}

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
	 * Returns the page instance specified by the {@link PageInfo} object. When there is no such
	 * page instance this method either throws {@link PageExpiredException} (if the
	 * <code>throwExpiredExceptionIfNotFound</code> parameter is true) or return <code>null>/code>
	 * 
	 * @param info
	 * @param throwExpiredExceptionIfNotFound
	 * @return page instance or <code>null</code>
	 */
	protected IPage getPageInstance(PageInfo info, boolean throwExpiredExceptionIfNotFound)
	{
		if (info == null)
		{
			throw new IllegalArgumentException("Argument 'info' may not be null.");
		}
		IPage page = getContext().getPageInstance(info.getPageMapName(), info.getPageId(),
			info.getVersionNumber());

		if (page == null && throwExpiredExceptionIfNotFound)
		{
			throw new PageExpiredException("Page expired.");
		}

		return page;
	}

	/**
	 * Returns the page instance specified by the {@link PageInfo} object. If there is no such page
	 * instance {@link PageExpiredException} is thrown.
	 * 
	 * @param info
	 * @return page instance
	 */
	protected IPage getPageInstance(PageInfo info)
	{
		return getPageInstance(info, true);
	}

	/**
	 * Returns component on page with given path. If the component is not found an exception is
	 * thrown.
	 * 
	 * @param page
	 * @param componentPath
	 * @return component instance
	 */
	protected IComponent getComponent(IPage page, String componentPath)
	{
		if (page == null)
		{
			throw new IllegalArgumentException("Argument 'page' may not be nul.");
		}
		if (componentPath == null)
		{
			throw new IllegalArgumentException("Argument 'componentPath' may not be null.");
		}
		IComponent component = page.get(componentPath);
		if (component == null)
		{
			throw new WicketRuntimeException("Component with path " + componentPath +
				" not found in page.");
		}
		else
		{
			return component;
		}
	}

	/**
	 * Loads page class with given name.
	 * 
	 * @param name
	 * @return class
	 */
	protected Class<? extends IPage> getPageClass(String name)
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Argument 'name' may not be null");
		}
		return Classes.resolveClass(name);
	}

	/**
	 * Extracts {@link PageParameters} from the URL using the given {@link PageParametersEncoder}.
	 * 
	 * @param url
	 * @param requestParameters
	 * @param segmentsToSkip
	 *            how many URL segments should be skipped because they "belong" to the
	 *            {@link RequestHandlerEncoder}
	 * @param encoder
	 * @return PageParameters instance
	 */
	protected PageParameters extractPageParameters(Url url, RequestParameters requestParameters,
		int segmentsToSkip, PageParametersEncoder encoder)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument 'url' may not be null.");
		}
		if (encoder == null)
		{
			throw new IllegalArgumentException("Argument 'encoder' may not be null.");
		}

		// strip the segments and first query parameter from URL
		Url urlCopy = new Url(url);
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

		PageParameters decoded = encoder.decodePageParameters(urlCopy, requestParameters);
		return decoded != null ? decoded : new PageParameters();
	}

	/**
	 * Encodes the given {@link PageParameters} to the URL using the given
	 * {@link PageParametersEncoder}. The original URL object is unchanged.
	 * 
	 * @param url
	 * @param pageParameters
	 * @param encoder
	 * @return URL with encoded parameters
	 */
	protected Url encodePageParameters(Url url, PageParameters pageParameters,
		PageParametersEncoder encoder)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument 'url' may no be null.");
		}

		if (pageParameters == null)
		{
			pageParameters = new PageParameters();
		}

		if (encoder == null)
		{
			throw new IllegalArgumentException("Argument 'encoder' may not be null.");
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
	 * Creates new page instance with the specified attributes.
	 * 
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @return new page instance
	 */
	protected IPage newPageInstance(String pageMapName, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{
		return getContext().newPageInstance(pageMapName, pageClass, pageParameters);
	}

	/**
	 * Tries to get existing page instance specified by the {@link PageInfo} object. If there is no
	 * such instance or there instance class doesn't match the <code>pageClass</code> argument,
	 * new page instance is created.
	 * 
	 * @param pageInfo
	 * @param pageClass
	 * @param pageParameters
	 * 
	 * @return page instance
	 */
	protected IPage getPageInstance(PageInfo pageInfo, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{
		return getPageInstance(pageInfo, pageClass, pageParameters, false);
	}

	/**
	 * Tries to get existing page instance specified by the {@link PageInfo} object. If there is no
	 * such instance or there instance class doesn't match the <code>pageClass</code> argument,
	 * new page instance is created.
	 * <p>
	 * If new page was created (no existing page was found) and the the
	 * <code>prepareForRenderNewPage</code> flag is set, <code>page.repareForRender(false)</code>
	 * is called on the page. This is necessary to do when looking on a component on stateless page
	 * as the component hierarchy has to be built.
	 * 
	 * @param pageInfo
	 * @param pageClass
	 * @param pageParameters
	 * @param prepareForRenderNewPage
	 * 
	 * @return page instance
	 */
	protected IPage getPageInstance(PageInfo pageInfo, Class<? extends IPage> pageClass,
		PageParameters pageParameters, boolean prepareForRenderNewPage)
	{
		if (pageInfo == null)
		{
			throw new IllegalArgumentException("Argument 'pageInfo' may not be null.");
		}
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Argument 'pageClass' may not be null.");
		}
		if (pageParameters == null)
		{
			throw new IllegalArgumentException("Argument 'pageParameters' may not be null.");
		}
		IPage page = getContext().getPageInstance(pageInfo.getPageMapName(), pageInfo.getPageId(),
			pageInfo.getVersionNumber());
		if (page != null && page.getClass().equals(pageClass) == false)
		{
			page = null;
		}
		if (page == null)
		{
			page = getContext().newPageInstance(pageInfo.getPageMapName(), pageClass,
				pageParameters);

			// this is required for stateless listeners
			if (prepareForRenderNewPage && page instanceof Page)
			{
				((Page)page).prepareForRender(false);
			}
		}
		else
		{
			page.getPageParameters().assign(pageParameters);
		}
		return page;
	}
}
