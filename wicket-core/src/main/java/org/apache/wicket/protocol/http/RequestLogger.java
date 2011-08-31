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
package org.apache.wicket.protocol.http;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters.NamedPair;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the logger class that can be set in the
 * {@link org.apache.wicket.protocol.http.WebApplication#getRequestLogger()} method. If this class
 * is set all request and live sessions will be recorded and displayed From the total created
 * sessions, to the peak session count and the current live sessions. For the live sessions the
 * request logger will record what request are happening what kind of {@link IRequestHandler} was
 * the event target and what {@link IRequestHandler} was the response target. It also records what
 * session data was touched for this and how long the request did take.
 * 
 * To view this information live see the {@link InspectorBug} that shows the {@link InspectorPage}
 * with the {@link LiveSessionsPage}.
 * 
 * This implementation uses a rounded buffer for storing the request data, and strives to minimize
 * contention on accessing the rounded buffer. At the beginning of your application start, the
 * buffer is empty and fills up during the lifetime of the application until the window size has
 * been reached, and new requests are written to the position containing the oldest request.
 * 
 * @since 1.2
 */
public class RequestLogger extends AbstractRequestLogger
{
	/** log, don't change this as it is often used to direct request logging to a different file. */
	private static final Logger LOG = LoggerFactory.getLogger(RequestLogger.class);

	@Override
	protected void log(RequestData rd, SessionData sd)
	{
		if (LOG.isInfoEnabled())
		{
			LOG.info(createRequestData(rd, sd));
		}
	}

	private String createRequestData(RequestData rd, SessionData sd)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(150);

		sb.append("startTime=\"");
		sb.append(formatDate(rd.getStartDate()));
		sb.append("\",duration=");
		sb.append(rd.getTimeTaken());
		sb.append(",url=\"");
		sb.append(escape('"', "\\\"", rd.getRequestedUrl()));
		sb.append("\"");
		sb.append(",event={");
		sb.append(getRequestHandlerString(rd.getEventTarget()));
		sb.append("},response={");
		sb.append(getRequestHandlerString(rd.getResponseTarget()));
		sb.append("},sessionid=\"");
		sb.append(rd.getSessionId());
		sb.append("\"");
		sb.append(",sessionsize=");
		sb.append(rd.getSessionSize());
		if (rd.getSessionInfo() != null && !Strings.isEmpty(rd.getSessionInfo().toString()))
		{
			sb.append(",sessioninfo={");
			sb.append(rd.getSessionInfo());
			sb.append("}");
		}
		if (sd != null)
		{
			sb.append(",sessionstart=\"");
			sb.append(formatDate(sd.getStartDate()));
			sb.append("\",requests=");
			sb.append(sd.getNumberOfRequests());
			sb.append(",totaltime=");
			sb.append(sd.getTotalTimeTaken());
		}
		sb.append(",activerequests=");
		sb.append(rd.getActiveRequest());

		Runtime runtime = Runtime.getRuntime();
		long max = runtime.maxMemory() / 1000000;
		long total = runtime.totalMemory() / 1000000;
		long used = total - runtime.freeMemory() / 1000000;
		sb.append(",maxmem=");
		sb.append(max);
		sb.append("M,total=");
		sb.append(total);
		sb.append("M,used=");
		sb.append(used);
		sb.append("M");

		return sb.toString();
	}

	private String escape(char replacable, String replacement, String value)
	{
		int pos = value.indexOf(replacable);
		if (pos == -1)
			return value;
		return value.substring(0, pos) + replacement +
			escape(replacable, replacement, value.substring(pos + 1));
	}

	private String getRequestHandlerString(IRequestHandler handler)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(128);
		if (handler != null)
		{
			try
			{
				sb.append("handler=");
				sb.append(handler.getClass().isAnonymousClass() ? handler.getClass().getName()
					: handler.getClass().getSimpleName());
				sb.append(",");
				if (handler instanceof AjaxRequestTarget)
				{
					getAjaxString(sb, (AjaxRequestTarget)handler);
				}
				else if (handler instanceof BookmarkablePageRequestHandler)
				{
					getBookmarkableString(sb, (BookmarkablePageRequestHandler)handler);
				}
				else if (handler instanceof BufferedResponseRequestHandler)
				{
					// nothing extra to log... BufferedResponse doesn't have identifiable
					// information about which request was buffered
				}
				else if (handler instanceof IRequestHandlerDelegate)
				{
					getDelegateString(sb, (IRequestHandlerDelegate)handler);
				}
				else if (handler instanceof ListenerInterfaceRequestHandler)
				{
					getListenerString(sb, (ListenerInterfaceRequestHandler)handler);
				}
				else if (handler instanceof RenderPageRequestHandler)
				{
					getRendererString(sb, (RenderPageRequestHandler)handler);
				}
				else if (handler instanceof ResourceReferenceRequestHandler)
				{
					getResourceString(sb, (ResourceReferenceRequestHandler)handler);
				}
				else if (handler instanceof ResourceStreamRequestHandler)
				{
					getResourceString(sb, (ResourceStreamRequestHandler)handler);
				}
			}
			catch (Exception x)
			{
				LOG.warn(
					"An error occurred during construction of the log entry for '{}', because of: {}",
					handler, x.getMessage());
				sb.append("UNKNOWN");
			}
		}
		else
		{
			sb.append("none");
		}
		return sb.toString();
	}

	private void getAjaxString(AppendingStringBuffer sb, AjaxRequestTarget ajaxHandler)
	{
		sb.append("pageClass=");
		sb.append(ajaxHandler.getPageClass().getName());
		sb.append(",");
		getPageParametersString(sb, ajaxHandler.getPageParameters());
		sb.append(",pageId=");
		sb.append(ajaxHandler.getPage().getId());
	}

	private void getBookmarkableString(AppendingStringBuffer sb,
		BookmarkablePageRequestHandler pageRequestHandler)
	{
		sb.append("pageClass=");
		sb.append(pageRequestHandler.getPageClass().getName());
		sb.append(",");
		getPageParametersString(sb, pageRequestHandler.getPageParameters());
	}

	private void getDelegateString(AppendingStringBuffer sb, IRequestHandlerDelegate delegateHandler)
	{
		sb.append("delegatedHandler=");
		sb.append(getRequestHandlerString(delegateHandler.getDelegateHandler()));
	}

	private void getListenerString(AppendingStringBuffer sb,
		ListenerInterfaceRequestHandler listener)
	{
		sb.append("pageClass=");
		sb.append(listener.getPageClass().getName());
		sb.append(",pageId=");
		sb.append(listener.getPage().getId());
		sb.append(",componentClass=");
		sb.append(listener.getComponent().getClass().getName());
		sb.append(",componentPath=");
		sb.append(listener.getComponent().getPageRelativePath());
		sb.append(",behaviorIndex=");
		sb.append(listener.getBehaviorIndex());
		sb.append(",behaviorClass=");
		if (listener.getBehaviorIndex() == null)
			sb.append("null");
		else
			sb.append(listener.getComponent()
				.getBehaviorById(listener.getBehaviorIndex())
				.getClass()
				.getName());
		sb.append(",interfaceName=");
		sb.append(listener.getListenerInterface().getName());
		sb.append(",interfaceMethod=");
		sb.append(listener.getListenerInterface().getMethod().getName());
	}

	private void getRendererString(AppendingStringBuffer sb,
		RenderPageRequestHandler pageRequestHandler)
	{
		sb.append("pageClass=");
		sb.append(pageRequestHandler.getPageClass().getName());
		sb.append(",");
		getPageParametersString(sb, pageRequestHandler.getPageParameters());
		IPageProvider pageProvider = pageRequestHandler.getPageProvider();
		if (!pageProvider.isNewPageInstance())
		{
			sb.append(",pageId=");
			sb.append(pageRequestHandler.getPage().getId());
		}
	}

	private void getResourceString(AppendingStringBuffer sb, ResourceStreamRequestHandler handler)
	{
		sb.append("fileName=\"");
		sb.append(escape('"', "\\\"", handler.getFileName()));
		sb.append("\",contentDisposition=");
		sb.append(handler.getContentDisposition());
		sb.append(",resourceStream={");
		IResourceStream resourceStream = handler.getResourceStream();
		if (resourceStream != null)
		{
			sb.append("resourceStreamClass=");
			sb.append(resourceStream.getClass().getName());
			sb.append(",contentType=\"");
			sb.append(resourceStream.getContentType());
			sb.append("\",locale=");
			sb.append(resourceStream.getLocale());
			sb.append(",style=");
			sb.append(resourceStream.getStyle());
			sb.append(",variation=");
			sb.append(resourceStream.getVariation());
		}
		sb.append("}");
	}

	private void getResourceString(AppendingStringBuffer sb,
		ResourceReferenceRequestHandler resourceRefenceHandler)
	{
		ResourceReference resourceReference = resourceRefenceHandler.getResourceReference();
		sb.append("resourceReferenceClass=");
		sb.append(resourceReference.getClass().getName());
		sb.append(",scope=");
		sb.append(resourceReference.getScope() != null ? resourceReference.getScope().getName()
			: "null");
		sb.append(",name=");
		sb.append(resourceReference.getName());
		sb.append(",locale=");
		sb.append(resourceReference.getLocale());
		sb.append(",style=");
		sb.append(resourceReference.getStyle());
		sb.append(",variation=");
		sb.append(resourceReference.getVariation());
	}

	private void getPageParametersString(AppendingStringBuffer sb, PageParameters parameters)
	{
		if (parameters != null)
		{
			sb.append("pageParameters={");
			String comma = "";
			for (int i = 0; i < parameters.getIndexedCount(); i++)
			{
				sb.append(comma);
				comma = ",";
				sb.append(i);
				sb.append("=\"");
				sb.append(escape('"', "\"", parameters.get(i).toString("")));
				sb.append("\"");
			}
			for (NamedPair pair : parameters.getAllNamed())
			{
				sb.append(comma);
				comma = ",";
				sb.append(pair.getKey());
				sb.append("=\"");
				sb.append(escape('"', "\"", pair.getValue()));
				sb.append("\"");
			}
			sb.append("}");
		}
	}
}
