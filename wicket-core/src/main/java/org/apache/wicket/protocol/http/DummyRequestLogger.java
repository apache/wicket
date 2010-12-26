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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.apache.wicket.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.AppendingStringBuffer;
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
 * with the {@link LiveSessionsPage}
 * 
 * @author jcompagner
 * 
 * @since 1.2
 */
public class DummyRequestLogger implements IRequestLogger
{
	/** log. */
	protected static Logger log = LoggerFactory.getLogger(DummyRequestLogger.class);

	private static MetaDataKey<RequestData> REQUEST_DATA = new MetaDataKey<RequestData>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final AtomicInteger totalCreatedSessions = new AtomicInteger();

	private final AtomicInteger peakSessions = new AtomicInteger();

	private final List<RequestData> requests;

	private final Map<String, SessionData> liveSessions;

	private final ThreadLocal<RequestData> currentRequest = new ThreadLocal<RequestData>();

	private final AtomicInteger activeRequests = new AtomicInteger();

	/**
	 * Construct.
	 */
	public DummyRequestLogger()
	{
		requests = Collections.synchronizedList(new LinkedList<RequestData>()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see java.util.LinkedList#add(java.lang.Object)
			 */
			@Override
			public void add(int index, RequestData o)
			{
				super.add(index, o);
				if (size() > Application.get().getRequestLoggerSettings().getRequestsWindowSize())
				{
					removeLast();
				}
			}
		});
		liveSessions = new ConcurrentHashMap<String, SessionData>();

	}

	public int getCurrentActiveRequestCount()
	{
		return activeRequests.get();
	}

	public SessionData[] getLiveSessions()
	{
		final SessionData[] sessions = liveSessions.values().toArray(new SessionData[0]);
		Arrays.sort(sessions);
		return sessions;
	}

	public int getPeakSessions()
	{
		return peakSessions.get();
	}

	public List<RequestData> getRequests()
	{
		return Collections.unmodifiableList(requests);
	}

	public int getTotalCreatedSessions()
	{
		return totalCreatedSessions.get();
	}

	public void logEventTarget(IRequestHandler requestHandler)
	{
		getCurrentRequest().addEventTarget(getRequestHandlerString(requestHandler));
	}

	public void logResponseTarget(IRequestHandler requestHandler)
	{
		getCurrentRequest().addResponseTarget(getRequestHandlerString(requestHandler));
	}

	/**
	 * @param requestHandler
	 * @return The request target nice display string
	 */
	private String getRequestHandlerString(final IRequestHandler requestHandler)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(128);
		if (requestHandler instanceof ListenerInterfaceRequestHandler)
		{
			ListenerInterfaceRequestHandler listener = (ListenerInterfaceRequestHandler)requestHandler;
			sb.append("Interface[component: ");
			sb.append(Classes.simpleName(listener.getComponent().getClass()));
			sb.append("(");
			sb.append(listener.getComponent().getPageRelativePath());
			sb.append("), page: ");
			sb.append(listener.getPage().getClass().getName());
			sb.append("(");
			sb.append(listener.getPage().getId());
			sb.append("), interface: ");
			sb.append(listener.getListenerInterface().getName());
			sb.append(".");
			sb.append(listener.getListenerInterface().getMethod().getName());
			sb.append("]");
		}
		else if (requestHandler instanceof BookmarkablePageRequestHandler)
		{
			BookmarkablePageRequestHandler pageRequestHandler = (BookmarkablePageRequestHandler)requestHandler;
			sb.append("BookmarkablePage[");
			sb.append(pageRequestHandler.getPageClass().getName());
			sb.append("(").append(pageRequestHandler.getPageParameters()).append(")");
			sb.append("]");
		}
		else if (requestHandler instanceof IPageRequestHandler)
		{
			IPageRequestHandler pageRequestHandler = (IPageRequestHandler)requestHandler;
			sb.append("PageRequest[");
			sb.append(pageRequestHandler.getPage().getClass().getName());
			sb.append("(");
			sb.append(pageRequestHandler.getPage().getId());
			sb.append(")]");
		}
		else if (requestHandler instanceof ResourceReferenceRequestHandler)
		{
			ResourceReferenceRequestHandler resourceRefenceHandler = (ResourceReferenceRequestHandler)requestHandler;
			sb.append("ResourceReference[");
			sb.append(resourceRefenceHandler.getResourceReference());
			sb.append("]");
		}
		else
		{
			sb.append(requestHandler.toString());
		}
		return sb.toString();
	}


	public void objectCreated(Object value)
	{
		RequestData rd = getCurrentRequest();

		if (value instanceof Session)
		{
			rd.addEntry("Session created");
		}
		else if (value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page created, id: " + page.getId() + ", class:" + page.getClass());
		}
		else
		{
			rd.addEntry("Custom object created: " + value);
		}

	}

	public void objectRemoved(Object value)
	{
		RequestData rd = getCurrentRequest();
		if (value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page removed, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if (value instanceof WebSession)
		{
			rd.addEntry("Session removed");
		}
		else
		{
			rd.addEntry("Custom object removed: " + value);
		}
	}

	public void objectUpdated(Object value)
	{
		RequestData rd = getCurrentRequest();
		if (value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page updated, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if (value instanceof Session)
		{
			rd.addEntry("Session updated");
		}
		else
		{
			rd.addEntry("Custom object updated: " + value);
		}
	}

	public void requestTime(long timeTaken)
	{
		RequestData rd = RequestCycle.get().getMetaData(REQUEST_DATA);
		if (rd != null)
		{
			if (activeRequests.get() > 0)
			{
				rd.setActiveRequest(activeRequests.decrementAndGet());
			}
			Session session = Session.get();
			String sessionId = session.getId();
			rd.setSessionId(sessionId);

			Object sessionInfo = getSessionInfo(session);
			rd.setSessionInfo(sessionInfo);

			long sizeInBytes = -1;
			if (Application.get().getRequestLoggerSettings().getRecordSessionSize())
			{
				try
				{
					sizeInBytes = session.getSizeInBytes();
				}
				catch (Exception e)
				{
					// log the error and let the request logging continue (this is what happens in
					// the
					// detach phase of the request cycle anyway. This provides better diagnostics).
					log.error(
						"Exception while determining the size of the session in the request logger: " +
							e.getMessage(), e);
				}
			}
			rd.setSessionSize(sizeInBytes);
			rd.setTimeTaken(timeTaken);

			requests.add(0, rd);
			if (sessionId != null)
			{
				SessionData sd = liveSessions.get(sessionId);
				if (sd == null)
				{
					// passivated session or logger only started after it.
					sessionCreated(sessionId);
					sd = liveSessions.get(sessionId);
				}
				if (sd != null)
				{
					sd.setSessionInfo(sessionInfo);
					sd.setSessionSize(sizeInBytes);
					sd.addTimeTaken(timeTaken);
					log(rd, sd);
				}
				else
				{
					log(rd, null);
				}
			}
			else
			{
				log(rd, null);
			}
		}
	}

	public void sessionCreated(String sessionId)
	{
		liveSessions.put(sessionId, new SessionData(sessionId));
		if (liveSessions.size() > peakSessions.get())
		{
			peakSessions.set(liveSessions.size());
		}
		totalCreatedSessions.incrementAndGet();
	}

	public void sessionDestroyed(String sessionId)
	{
		liveSessions.remove(sessionId);
	}

	RequestData getCurrentRequest()
	{
		RequestCycle requestCycle = RequestCycle.get();
		RequestData rd = requestCycle.getMetaData(REQUEST_DATA);
		if (rd == null)
		{
			rd = new RequestData();
			requestCycle.setMetaData(REQUEST_DATA, rd);
			activeRequests.incrementAndGet();
		}
		return rd;
	}

	/**
	 * @param rd
	 * @param sd
	 */
	protected void log(RequestData rd, SessionData sd)
	{
		if (log.isInfoEnabled())
		{
			log.info(createLogString(rd, sd, true).toString());
		}
	}

	protected final AppendingStringBuffer createLogString(RequestData rd, SessionData sd,
		boolean includeRuntimeInfo)
	{
		AppendingStringBuffer asb = new AppendingStringBuffer(150);
		asb.append("time=");
		asb.append(rd.getTimeTaken());
		asb.append(",event=");
		asb.append(rd.getEventTarget());
		asb.append(",response=");
		asb.append(rd.getResponseTarget());
		if (rd.getSessionInfo() != null && !rd.getSessionInfo().equals(""))
		{
			asb.append(",sessioninfo=");
			asb.append(rd.getSessionInfo());
		}
		else
		{
			asb.append(",sessionid=");
			asb.append(rd.getSessionId());
		}
		asb.append(",sessionsize=");
		asb.append(rd.getSessionSize());
		if (sd != null)
		{
			asb.append(",sessionstart=");
			asb.append(sd.getStartDate());
			asb.append(",requests=");
			asb.append(sd.getNumberOfRequests());
			asb.append(",totaltime=");
			asb.append(sd.getTotalTimeTaken());
		}
		asb.append(",activerequests=");
		asb.append(rd.getActiveRequest());
		if (includeRuntimeInfo)
		{
			Runtime runtime = Runtime.getRuntime();
			long max = runtime.maxMemory() / 1000000;
			long total = runtime.totalMemory() / 1000000;
			long used = total - runtime.freeMemory() / 1000000;
			asb.append(",maxmem=");
			asb.append(max);
			asb.append("M,total=");
			asb.append(total);
			asb.append("M,used=");
			asb.append(used);
			asb.append("M");
		}
		return asb;
	}

	private Object getSessionInfo(Session session)
	{
		if (session instanceof ISessionLogInfo)
		{
			return ((ISessionLogInfo)session).getSessionInfo();
		}
		return "";
	}

}
