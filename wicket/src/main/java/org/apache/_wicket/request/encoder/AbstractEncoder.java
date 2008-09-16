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
import org.apache._wicket.request.RequestHandlerEncoder;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.Url.QueryParameter;
import org.apache.wicket.RequestListenerInterface;
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
}
