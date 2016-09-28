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

import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.util.lang.Classes;
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
 * To view this information live see org.apache.wicket.devutils.inspector.InspectorBug that shows
 * the org.apache.wicket.devutils.inspector.InspectorPage with the org.apache.wicket.devutils.inspector.LiveSessionsPage.
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
		sb.append(rd.getRequestedUrl());
		sb.append('"');
		sb.append(",event={");
		sb.append(getRequestHandlerString(rd.getEventTarget()));
		sb.append("},response={");
		sb.append(getRequestHandlerString(rd.getResponseTarget()));
		sb.append("},sessionid=\"");
		sb.append(rd.getSessionId());
		sb.append('"');
		sb.append(",sessionsize=");
		sb.append(rd.getSessionSize());
		if (rd.getSessionInfo() != null && !Strings.isEmpty(rd.getSessionInfo().toString()))
		{
			sb.append(",sessioninfo={");
			sb.append(rd.getSessionInfo());
			sb.append('}');
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
		sb.append('M');

		return sb.toString();
	}

	private String getRequestHandlerString(IRequestHandler handler)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(128);
		if (handler != null)
		{
			Class<? extends IRequestHandler> handlerClass = handler.getClass();
			sb.append("handler=");
			sb.append(handlerClass.isAnonymousClass() ? handlerClass.getName()
					: Classes.simpleName(handlerClass));
			if (handler instanceof ILoggableRequestHandler)
			{
				sb.append(",data=");
				sb.append(((ILoggableRequestHandler)handler).getLogData());
			}
		}
		else
		{
			sb.append("none");
		}
		return sb.toString();
	}
}
