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
import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.util.string.Strings;

/**
 * Convenience class for implementing encoders.
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

	protected String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
	{
		return getContext().requestListenerInterfaceToString(listenerInterface);
	}

	protected RequestListenerInterface requestListenerInterfaceFromString(String interfaceName)
	{
		return getContext().requestListenerInterfaceFromString(interfaceName);
	}

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
					if (segments[i].equals(url.getSegments().get(i)) == false)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	protected PageComponentInfo getPageComponentInfo(Url url)
	{
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

	protected IPage getPageInstance(PageInfo info, boolean throwExpiredExceptionIfNotFound)
	{
		IPage page = getContext().getPageInstance(info.getPageMapName(), info.getPageId(),
			info.getVersionNumber());

		if (page == null && throwExpiredExceptionIfNotFound)
		{
			throw new PageExpiredException("Page expired.");
		}

		return page;
	}

	protected IPage getPageInstance(PageInfo info)
	{
		return getPageInstance(info, true);
	}

	protected IComponent getComponent(IPage page, String componentPath)
	{
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

	@SuppressWarnings("unchecked")
	protected Class<? extends IPage> getPageClass(String name)
	{
		try
		{			
			if (Session.exists())				
			{
				Session s = Session.get();
				return (Class<? extends IPage>)s.getClassResolver().resolveClass(name);

			}
			else
			{
				return (Class<? extends IPage>)Class.forName(name);
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new WicketRuntimeException("Error resolving bookmarkable page class", e);
		}
	}

	protected PageParameters extractPageParameters(Url url, RequestParameters requestParameters,
		int segmentsToSkip, PageParametersEncoder encoder)
	{
		// strip the segments and first query parameter from URL
		Url urlCopy = new Url(url);
		while (segmentsToSkip > 0 && urlCopy.getSegments().isEmpty() == false)
		{
			urlCopy.getSegments().remove(0);
			--segmentsToSkip;
		}

		if (getPageComponentInfo(urlCopy) != null)
		{
			urlCopy.getQueryParameters().remove(0);
		}

		PageParameters decoded = encoder.decodePageParameters(urlCopy);
		return decoded != null ? decoded : new PageParameters();
	}

	protected Url encodePageParameters(Url url, PageParameters pageParameters,
		PageParametersEncoder encoder)
	{
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

	protected IPage newPageInstance(String pageMapName, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{
		return getContext().newPageInstance(pageMapName, pageClass, pageParameters);
	}

	protected IPage getPageInstance(PageInfo pageInfo, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{
		return getPageInstance(pageInfo, pageClass, pageParameters, false);
	}

	protected IPage getPageInstance(PageInfo pageInfo, Class<? extends IPage> pageClass,
		PageParameters pageParameters, boolean prepareForRenderNewPage)
	{
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

	protected void encodePageComponentInfo(Url url, PageComponentInfo info)
	{
		String s = info.toString();
		if (!Strings.isEmpty(s))
		{
			QueryParameter parameter = new QueryParameter(s, "");
			url.getQueryParameters().add(parameter);
		}
	}
}
