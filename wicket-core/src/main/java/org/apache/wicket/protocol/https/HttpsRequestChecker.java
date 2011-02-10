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
package org.apache.wicket.protocol.https;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.IPageClassRequestHandler;

/**
 * A helper class which will replace the current {@link IRequestHandler request handler} with
 * {@link SwitchProtocolRequestHandler} if the page that is going to be rendered is annotated with @
 * {@link RequireHttps}.
 */
class HttpsRequestChecker
{

	/**
	 * 
	 * @param requestHandler
	 *            the original request handler
	 * @param httpsConfig
	 *            the https configuration
	 * @return either {@link SwitchProtocolRequestHandler} if the page that is going to be rendered
	 *         is annotated with @{@link RequireHttps} and the protocol of the current request is
	 *         http, or will return the original handler if these conditions are not fulfilled
	 */
	IRequestHandler checkSecureIncoming(IRequestHandler requestHandler,
		final HttpsConfig httpsConfig)
	{

		if (requestHandler instanceof SwitchProtocolRequestHandler)
		{
			return requestHandler;
		}

		Class<?> pageClass = getPageClass(requestHandler);
		if (pageClass != null)
		{
			final IRequestHandler redirect;

			if (hasSecureAnnotation(pageClass))
			{
				redirect = SwitchProtocolRequestHandler.requireProtocol(Protocol.HTTPS, httpsConfig);
			}
			else
			{
				redirect = SwitchProtocolRequestHandler.requireProtocol(Protocol.HTTP, httpsConfig);
			}

			if (redirect != null)
			{
				return redirect;
			}

		}
		return requestHandler;
	}

	/**
	 * Figures out the protocol that should be used for a given request handler
	 * 
	 * @param requestHandler
	 * @return protocol
	 */
	public Protocol getProtocol(IRequestHandler requestHandler)
	{
		Class<?> pageClass = getPageClass(requestHandler);
		if (pageClass != null)
		{
			if (hasSecureAnnotation(pageClass))
			{
				return Protocol.HTTPS;
			}
			else
			{
				return Protocol.HTTP;
			}
		}
		else
		{
			return Protocol.PRESERVE_CURRENT;
		}
	}

	/**
	 * Checks if the class has a {@link RequireHttps} annotation
	 * 
	 * @param klass
	 * @return true if klass has the annotation
	 */
	private boolean hasSecureAnnotation(Class<?> klass)
	{
		for (Class<?> c : klass.getInterfaces())
		{
			if (hasSecureAnnotation(c))
			{
				return true;
			}
		}
		if (klass.getAnnotation(RequireHttps.class) != null)
		{
			return true;
		}

		if (klass.getSuperclass() != null)
		{
			return hasSecureAnnotation(klass.getSuperclass());
		}
		return false;
	}

	/**
	 * Gets page class from a request handler
	 * 
	 * @param handler
	 * @return page class if there is one, null otherwise
	 */
	private Class<?> getPageClass(IRequestHandler handler)
	{
		if (handler instanceof IPageClassRequestHandler)
		{
			return ((IPageClassRequestHandler)handler).getPageClass();
		}
		else
		{
			return null;
		}
	}

}
