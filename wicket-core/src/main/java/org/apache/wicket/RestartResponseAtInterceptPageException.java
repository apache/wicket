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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.IWritableRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.Url.QueryParameter;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.ResetResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * Causes Wicket to interrupt current request processing and immediately redirect to an intercept
 * page.
 */
public class RestartResponseAtInterceptPageException extends ResetResponseException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Redirects to the specified {@code interceptPage}.
	 * 
	 * @param interceptPage
	 */
	public RestartResponseAtInterceptPageException(Page interceptPage)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPage),
			RedirectPolicy.AUTO_REDIRECT));
		InterceptData.set();
	}

	/**
	 * Redirects to the specified intercept page, this will result in a bookmarkable redirect.
	 * 
	 * @param interceptPageClass
	 */
	public RestartResponseAtInterceptPageException(Class<? extends Page> interceptPageClass)
	{
		this(interceptPageClass, null);
	}

	/**
	 * Redirects to the specified intercept page, this will result in a bookmarkable redirect.
	 * 
	 * @param interceptPageClass
	 * @param parameters
	 */
	public RestartResponseAtInterceptPageException(Class<? extends Page> interceptPageClass,
		PageParameters parameters)
	{
		super(new RenderPageRequestHandler(new PageProvider(interceptPageClass, parameters),
			RedirectPolicy.ALWAYS_REDIRECT));
		InterceptData.set();
	}

	/**
	 * @return the url of the request when the interception happened or {@code null}
	 * or {@code null} if there was no interception yet
	 */
	public static Url getOriginalUrl()
	{
		Url originalUrl = null;
		InterceptData data = InterceptData.get();
		if (data != null)
		{
			originalUrl = data.getOriginalUrl();
		}
		return originalUrl;
	}

	/**
	 * @return the post parameters of thе request when the interception happened
	 * or {@code null} if there was no interception yet
	 */
	public static Map<String, List<StringValue>> getOriginalPostParameters()
	{
		Map<String, List<StringValue>> postParameters = null;
		InterceptData data = InterceptData.get();
		if (data != null)
		{
			postParameters = data.getPostParameters();
		}
		return postParameters;
	}

	/**
	 * Attaches data to the request that is being handled, to be remembered along with the
	 * destination should this request be intercepted. The request that later resumes that
	 * destination reads it back with {@link #getInterceptMetaData(MetaDataKey)}.
	 * <p>
	 * A request is not intercepted where it is interrupted: any code can throw this exception - a
	 * page constructor, a listener, the render of a page - and one request can be intercepted more
	 * than once, each interception replacing what the previous one remembered. Data attached here
	 * travels with the destination whichever of those happens, which data collected at a particular
	 * throw site cannot.
	 * <p>
	 * The data is remembered in the session, so it has to be serializable, and it is handed to every
	 * request that resumes the destination, so it should be immutable.
	 * 
	 * @param <T>
	 *            the type of the data
	 * @param key
	 *            the key to attach the data under
	 * @param data
	 *            the data, or {@code null} to remove what was attached under {@code key}
	 */
	public static <T> void setInterceptMetaData(MetaDataKey<T> key, T data)
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle != null)
		{
			cycle.setMetaData(pendingMetaDataKey,
				key.set(cycle.getMetaData(pendingMetaDataKey), data));
		}
	}

	/**
	 * The data that {@link #setInterceptMetaData(MetaDataKey, Object)} attached to the request whose
	 * destination the current request resumes.
	 * 
	 * @param <T>
	 *            the type of the data
	 * @param key
	 *            the key the data was attached under
	 * @return the data, or {@code null} when the current request resumes no destination, or when
	 *         nothing was attached under {@code key}
	 */
	public static <T> T getInterceptMetaData(MetaDataKey<T> key)
	{
		RequestCycle cycle = RequestCycle.get();
		return cycle == null ? null : key.get(cycle.getMetaData(resumedMetaDataKey));
	}

	/** What is attached to the request being handled, in case it is intercepted. */
	private static final MetaDataKey<MetaDataEntry<?>[]> pendingMetaDataKey = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** What was attached to the request whose destination the current request resumes. */
	private static final MetaDataKey<MetaDataEntry<?>[]> resumedMetaDataKey = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * INTERNAL CLASS, DO NOT USE
	 * 
	 * @author igor.vaynberg
	 */
	static class InterceptData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Url originalUrl;
		private Map<String, List<StringValue>> postParameters;
		private MetaDataEntry<?>[] metaData;

		public Url getOriginalUrl()
		{
			return originalUrl;
		}

		public Map<String, List<StringValue>> getPostParameters()
		{
			return postParameters;
		}

		public static void set()
		{
			Session session = Session.get();
			session.bind();
			InterceptData data = new InterceptData();
			RequestCycle cycle = RequestCycle.get();
			Request request = cycle.getRequest();
			data.metaData = cycle.getMetaData(pendingMetaDataKey);
			data.originalUrl = request.getOriginalUrl();
			Iterator<QueryParameter> itor = data.originalUrl.getQueryParameters().iterator();
			while (itor.hasNext())
			{
				QueryParameter parameter = itor.next();
				String parameterName = parameter.getName();
				if (WebRequest.PARAM_AJAX.equals(parameterName) ||
					WebRequest.PARAM_AJAX_BASE_URL.equals(parameterName) ||
					WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE.equals(parameterName))
				{
					itor.remove();
				}
			}

			data.postParameters = new HashMap<>();
			for (String s : request.getPostParameters().getParameterNames())
			{
				if (WebRequest.PARAM_AJAX.equals(s) || WebRequest.PARAM_AJAX_BASE_URL.equals(s) ||
					WebRequest.PARAM_AJAX_REQUEST_ANTI_CACHE.equals(s))
				{
					continue;
				}
				data.postParameters.put(s, new ArrayList<>(request.getPostParameters()
					.getParameterValues(s)));
			}
			session.setMetaData(key, data);
		}

		public static InterceptData get()
		{
			if (Session.exists())
			{
				return Session.get().getMetaData(key);
			}
			return null;
		}

		public static void clear()
		{
			if (Session.exists())
			{
				Session.get().setMetaData(key, null);
			}
		}

		private static final MetaDataKey<InterceptData> key = new MetaDataKey<>()
		{
			private static final long serialVersionUID = 1L;
		};
	}

	static void continueToOriginalDestination()
	{
		InterceptData data = InterceptData.get();
		if (data != null)
		{
			String url = RequestCycle.get().getUrlRenderer().renderUrl(data.originalUrl);
			throw new NonResettingRestartException(url);
		}
	}

	static void clearOriginalDestination()
	{
		InterceptData.clear();
	}

	static IRequestMapper MAPPER = new IRequestMapper()
	{
		@Override
		public int getCompatibilityScore(Request request)
		{
			return matchedData(request) != null ? Integer.MAX_VALUE : 0;
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler)
		{
			return null;
		}

		@Override
		public IRequestHandler mapRequest(Request request)
		{
			InterceptData data = matchedData(request);
			if (data != null)
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
				RequestCycle cycle = RequestCycle.get();
				if (cycle != null)
				{
					cycle.setMetaData(resumedMetaDataKey, data.metaData);
				}
				InterceptData.clear();
			}
			return null;
		}

		private InterceptData matchedData(Request request)
		{
			InterceptData data = InterceptData.get();
			if (data != null && data.originalUrl.equals(request.getOriginalUrl()))
			{
				return data;
			}
			return null;
		}
	};
}
