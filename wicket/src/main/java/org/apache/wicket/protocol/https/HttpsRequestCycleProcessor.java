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

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.https.SwitchProtocolRequestTarget.Protocol;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IPageRequestTarget;


/**
 * Request cycle processor that can switch between http and https protocols based on the
 * {@link RequireHttps} annotation.
 * 
 * Once this processor is installed, any page annotated with the {@link RequireHttps} annotation
 * will be served over https, while any page lacking the annotation will be served over http. The
 * annotation can be placed on a super class or an interface that a page implements.
 * 
 * To install this processor:
 * 
 * <pre>
 * class MyApplication extends WebApplication
 * {
 * 	&#064;Override
 * 	protected IRequestCycleProcessor newRequestCycleProcessor()
 * 	{
 * 		return new HttpsRequestCycleProcessor(config);
 * 	}
 * }
 * </pre>
 * 
 * <b>Notes</b>: According to servlet spec a cookie created on an https request is marked as secure,
 * such cookies are not available for http requests. What this means is that a session started over
 * https will not be propagated to further http calls because JSESSIONID cookie will be marked as
 * secure and not available to http requests. This entails that unless a session is created and
 * bound on http prior to using an https request any wicket pages or session values stored in the
 * https session will not be available to further http requests. If your application requires a
 * http-&gt;https-&gt;http interactions (such as the case where only a login page and my account
 * pages are secure) you must make sure a session is created and stored in the http request prior to
 * the first http-&gt;https redirect.
 */
public class HttpsRequestCycleProcessor extends WebRequestCycleProcessor
{
	private final HttpsConfig portConfig;

	/**
	 * Constructor
	 * 
	 * @param httpsConfig
	 *            configuration
	 */
	public HttpsRequestCycleProcessor(HttpsConfig httpsConfig)
	{
		portConfig = httpsConfig;
	}

	/**
	 * @return configuration
	 */
	public HttpsConfig getConfig()
	{
		return portConfig;
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
		else
		{
			return false;
		}
	}

	/**
	 * Gets page class from a request target
	 * 
	 * @param target
	 * @return page class if there is one, null otherwise
	 */
	private Class<?> getPageClass(IRequestTarget target)
	{
		if (target instanceof IPageRequestTarget)
		{
			return ((IPageRequestTarget)target).getPage().getClass();
		}
		else if (target instanceof IBookmarkablePageRequestTarget)
		{
			return ((IBookmarkablePageRequestTarget)target).getPageClass();
		}
		else
		{
			return null;
		}
	}

	/** @deprecated use checkSecureIncoming */
	@Deprecated
	protected IRequestTarget checkSecure(IRequestTarget target)
	{
		return checkSecureIncoming(target);
	}

	protected IRequestTarget checkSecureIncoming(IRequestTarget target)
	{

		if (target != null && target instanceof SwitchProtocolRequestTarget)
		{
			return target;
		}
		if (portConfig == null)
		{
			return target;
		}

		Class<?> pageClass = getPageClass(target);
		if (pageClass != null)
		{
			IRequestTarget redirect = null;
			if (hasSecureAnnotation(pageClass))
			{
				redirect = SwitchProtocolRequestTarget.requireProtocol(Protocol.HTTPS);
			}
			else
			{
				redirect = SwitchProtocolRequestTarget.requireProtocol(Protocol.HTTP);
			}
			if (redirect != null)
			{
				return redirect;
			}

		}
		return target;
	}

	protected IRequestTarget checkSecureOutgoing(IRequestTarget target)
	{

		if (target != null && target instanceof SwitchProtocolRequestTarget)
		{
			return target;
		}
		if (portConfig == null)
		{
			return target;
		}

		Class<?> pageClass = getPageClass(target);
		if (pageClass != null)
		{
			IRequestTarget redirect = null;
			if (hasSecureAnnotation(pageClass))
			{
				redirect = SwitchProtocolRequestTarget.requireProtocol(Protocol.HTTPS, target);
			}
			else
			{
				redirect = SwitchProtocolRequestTarget.requireProtocol(Protocol.HTTP, target);
			}
			if (redirect != null)
			{
				return redirect;
			}

		}
		return target;
	}


	/** {@inheritDoc} */
	@Override
	public IRequestTarget resolve(RequestCycle rc, RequestParameters rp)
	{
		if (portConfig.isPreferStateful())
		{
		// we need to persist the session before a redirect to https so the session lasts across
		// both http and https calls.
		Session.get().bind();
		}

		IRequestTarget target = super.resolve(rc, rp);
		return checkSecure(target);
	}

	/** {@inheritDoc} */
	@Override
	public void respond(RequestCycle requestCycle)
	{
		IRequestTarget requestTarget = requestCycle.getRequestTarget();
		if (requestTarget != null)
		{
			IRequestTarget secured = checkSecureOutgoing(requestTarget);
			if (secured != requestTarget)
			{
				requestCycle.setRequestTarget(secured);
				// respond will be called again because we called setrequesttarget(), so we do not
				// process it this time
				return;
			}
		}
		super.respond(requestCycle);
	}
}
