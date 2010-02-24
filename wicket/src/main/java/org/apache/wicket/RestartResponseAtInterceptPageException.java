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
package org.apache.wicket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ng.request.IRequestMapper;
import org.apache.wicket.ng.request.IWritableRequestParameters;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.request.target.basic.RedirectRequestHandler;
import org.apache.wicket.util.string.StringValue;

public class RestartResponseAtInterceptPageException extends AbstractRestartResponseException
{

	public RestartResponseAtInterceptPageException(Page interceptPage)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPage)));
		SessionData.set();
	}

	public RestartResponseAtInterceptPageException(Class<? extends Page> interceptPageClass)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPageClass)));
		SessionData.set();
	}

	private static class SessionData implements Serializable
	{
		private Url originalUrl;
		private Map<String, List<StringValue>> postParameters;
		private boolean continueOk;

		public static void set()
		{
			Session session = Session.get();
			session.bind();
			SessionData data = new SessionData();
			Request request = RequestCycle.get().getRequest();
			data.originalUrl = request.getOriginalUrl();
			data.postParameters = new HashMap<String, List<StringValue>>();
			for (String s : request.getPostParameters().getParameterNames())
			{
				data.postParameters.put(s, new ArrayList<StringValue>(request.getPostParameters()
					.getParameterValues(s)));
			}
			data.continueOk = false;
			session.setMetaData(key, data);
		}

		public static SessionData get()
		{
			Session session = Session.get();
			if (session != null)
			{
				return session.getMetaData(key);
			}
			else
			{
				return null;
			}
		}

		public static void clear()
		{
			Session session = Session.get();
			if (session != null)
			{
				session.setMetaData(key, null);
			}
		}

		private static MetaDataKey<SessionData> key = new MetaDataKey<SessionData>()
		{
			private static final long serialVersionUID = 1L;
		};
	};

	static boolean continueToOriginalDestination()
	{
		SessionData data = SessionData.get();
		if (data != null)
		{
			data.continueOk = true;
			String url = RequestCycle.get().getUrlRenderer().renderUrl(data.originalUrl);
			RequestCycle.get().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(url));
			return true;
		}
		return false;
	}

	public static IRequestMapper MAPPER = new IRequestMapper()
	{
		public int getCompatibilityScore(Request request)
		{
			return 0;
		}

		public Url mapHandler(IRequestHandler requestHandler)
		{
			return null;
		}

		public IRequestHandler mapRequest(Request request)
		{
			SessionData data = SessionData.get();
			if (data != null)
			{
				if (data.originalUrl.equals(request.getOriginalUrl()))
				{
					if (data.postParameters.isEmpty() == false &&
						request.getPostParameters() instanceof IWritableRequestParameters)
					{
						IWritableRequestParameters parameters = (IWritableRequestParameters)request.getPostParameters();
						parameters.reset();
						for (String s : data.postParameters.keySet())
						{
							parameters.setParameterValues(s, data.postParameters.get(s));
						}
					}
					SessionData.clear();
				}
			}
			return null;
		}
	};
}
