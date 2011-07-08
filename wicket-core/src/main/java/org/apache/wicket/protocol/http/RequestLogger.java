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

import static java.lang.System.arraycopy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IRequestLoggerSettings;
import org.apache.wicket.util.lang.Args;
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
public class RequestLogger implements IRequestLogger
{
	/** log, don't change this as it is often used to direct request logging to a different file. */
	protected static Logger log = LoggerFactory.getLogger(RequestLogger.class);

	/**
	 * Key for storing request data in the request cycle's meta data.
	 */
	private static MetaDataKey<RequestData> REQUEST_DATA = new MetaDataKey<RequestData>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Key for storing session data in the request cycle's meta data.
	 */
	private static MetaDataKey<SessionData> SESSION_DATA = new MetaDataKey<SessionData>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final AtomicInteger totalCreatedSessions = new AtomicInteger();

	private final AtomicInteger peakSessions = new AtomicInteger();

	private final Map<String, SessionData> liveSessions;

	private final AtomicInteger activeRequests = new AtomicInteger();

	private final AtomicInteger peakActiveRequests = new AtomicInteger();

	/**
	 * Rounded request buffer that keeps the request data for the last N requests in the buffer.
	 */
	private RequestData[] requestWindow;

	/**
	 * Cursor pointing to the current writable location in the buffer. Points to the first empty
	 * slot or if the buffer has been filled completely to the oldest request in the buffer.
	 */
	private int indexInWindow = 0;

	/**
	 * records the total request time across the sliding request window so that it can be used to
	 * calculate the average request time across the window duration.
	 */
	private long totalRequestTime = 0l;

	/**
	 * records the start time of the oldest request across the sliding window so that it can be used
	 * to calculate the average request time across the window duration.
	 */
	private Date startTimeOfOldestRequest;

	/**
	 * Construct.
	 */
	public RequestLogger()
	{
		int requestsWindowSize = Application.get()
			.getRequestLoggerSettings()
			.getRequestsWindowSize();
		requestWindow = new RequestData[requestsWindowSize];
		liveSessions = new ConcurrentHashMap<String, SessionData>();
	}

	public int getCurrentActiveRequestCount()
	{
		return activeRequests.get();
	}

	public int getPeakActiveRequestCount()
	{
		return peakActiveRequests.get();
	}

	public SessionData[] getLiveSessions()
	{
		final SessionData[] sessions = liveSessions.values().toArray(
			new SessionData[liveSessions.values().size()]);
		Arrays.sort(sessions);
		return sessions;
	}

	public int getPeakSessions()
	{
		return peakSessions.get();
	}

	public List<RequestData> getRequests()
	{
		synchronized (requestWindow)
		{
			RequestData[] result = new RequestData[hasBufferRolledOver() ? requestWindow.length
				: indexInWindow];
			copyRequestsInOrder(result);
			return Arrays.asList(result);
		}
	}

	/**
	 * Copies all request data into {@code copy} such that the oldest request is in slot 0 and the
	 * most recent request is in slot {@code copy.length}
	 * 
	 * @param copy
	 *            the target, has to have a capacity of at least {@code requestWindow.length}
	 */
	private void copyRequestsInOrder(RequestData[] copy)
	{
		Args.isTrue(copy.length >= requestWindow.length, "copy.length must be at least {}",
			requestWindow.length);
		if (hasBufferRolledOver())
		{
			// first copy the oldest requests stored behind the cursor into the copy
			int oldestPos = indexInWindow + 1;
			if (oldestPos < requestWindow.length)
				arraycopy(requestWindow, oldestPos, copy, 0, requestWindow.length - oldestPos);

			// then append the newer requests stored from index 0 til the cursor position.
			arraycopy(requestWindow, 0, copy, requestWindow.length - oldestPos, indexInWindow);
		}
		else
		{
			arraycopy(requestWindow, 0, copy, 0, indexInWindow);
		}
	}

	/**
	 * @return whether the buffer has been filled to capacity at least once
	 */
	private boolean hasBufferRolledOver()
	{
		return requestWindow[requestWindow.length - 1] != null;
	}

	public int getTotalCreatedSessions()
	{
		return totalCreatedSessions.get();
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
		RequestData requestdata = RequestCycle.get().getMetaData(REQUEST_DATA);
		if (requestdata != null)
		{
			if (activeRequests.get() > 0)
			{
				requestdata.setActiveRequest(activeRequests.decrementAndGet());
			}
			Session session = Session.get();
			String sessionId = session.getId();
			requestdata.setSessionId(sessionId);

			Object sessionInfo = getSessionInfo(session);
			requestdata.setSessionInfo(sessionInfo);

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
					// the detach phase of the request cycle anyway. This provides better
					// diagnostics).
					log.error(
						"Exception while determining the size of the session in the request logger: " +
							e.getMessage(), e);
				}
			}
			requestdata.setSessionSize(sizeInBytes);
			requestdata.setTimeTaken(timeTaken);

			addRequest(requestdata);

			SessionData sessiondata = null;
			if (sessionId != null)
			{
				sessiondata = liveSessions.get(sessionId);
				if (sessiondata == null)
				{
					// if the session has been destroyed during the request by
					// Session#invalidateNow, retrieve the old session data from the RequestCycle.
					sessiondata = RequestCycle.get().getMetaData(SESSION_DATA);
				}
				if (sessiondata == null)
				{
					// passivated session or logger only started after it.
					sessionCreated(sessionId);
					sessiondata = liveSessions.get(sessionId);
				}
				if (sessiondata != null)
				{
					sessiondata.setSessionInfo(sessionInfo);
					sessiondata.setSessionSize(sizeInBytes);
					sessiondata.addTimeTaken(timeTaken);
				}
			}
			// log the request- and sessiondata (the latter can be null)
			log(requestdata, sessiondata);
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
		RequestCycle requestCycle = RequestCycle.get();
		SessionData sessionData = liveSessions.remove(sessionId);
		if (requestCycle != null)
			requestCycle.setMetaData(SESSION_DATA, sessionData);
	}

	protected RequestData getCurrentRequest()
	{
		RequestCycle requestCycle = RequestCycle.get();
		RequestData rd = requestCycle.getMetaData(REQUEST_DATA);
		if (rd == null)
		{
			rd = new RequestData();
			requestCycle.setMetaData(REQUEST_DATA, rd);
			int activeCount = activeRequests.incrementAndGet();

			if (activeCount > peakActiveRequests.get())
			{
				peakActiveRequests.set(activeCount);
			}
		}
		return rd;
	}

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
		AppendingStringBuffer sb = new AppendingStringBuffer(150);
		sb.append("time=");
		sb.append(rd.getTimeTaken());
		if (!Strings.isEmpty(rd.getRequestedUrl()))
		{
			sb.append(",url=");
			sb.append(rd.getRequestedUrl());
		}
		sb.append(",event=");
		sb.append(rd.getEventTarget());
		sb.append(",response=");
		sb.append(rd.getResponseTarget());
		if (rd.getSessionInfo() != null && !Strings.isEmpty(rd.getSessionInfo().toString()))
		{
			sb.append(",sessioninfo=");
			sb.append(rd.getSessionInfo());
		}
		else
		{
			sb.append(",sessionid=");
			sb.append(rd.getSessionId());
		}
		sb.append(",sessionsize=");
		sb.append(rd.getSessionSize());
		if (sd != null)
		{
			sb.append(",sessionstart=");
			sb.append(sd.getStartDate());
			sb.append(",requests=");
			sb.append(sd.getNumberOfRequests());
			sb.append(",totaltime=");
			sb.append(sd.getTotalTimeTaken());
		}
		sb.append(",activerequests=");
		sb.append(rd.getActiveRequest());
		if (includeRuntimeInfo)
		{
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
		}
		return sb;
	}

	private Object getSessionInfo(Session session)
	{
		if (session instanceof ISessionLogInfo)
		{
			return ((ISessionLogInfo)session).getSessionInfo();
		}
		return "";
	}

	protected void addRequest(RequestData rd)
	{
		// ensure the buffer has the proper installed length
		resizeBuffer();

		synchronized (requestWindow)
		{
			// if the requestWindow is a zero-length array, nothing gets stored
			if (requestWindow.length == 0)
				return;

			// use the oldest request data to recalculate the average request time
			RequestData old = requestWindow[indexInWindow];

			// replace the oldest request with the nweset request
			requestWindow[indexInWindow] = rd;

			// move the cursor to the next writable position containing the oldest request or if the
			// buffer hasn't been filled completely the first empty slot
			indexInWindow = (indexInWindow + 1) % requestWindow.length;
			if (old != null)
			{
				startTimeOfOldestRequest = requestWindow[indexInWindow].getStartDate();
				totalRequestTime -= old.getTimeTaken();
			}
			else
			{
				if (startTimeOfOldestRequest == null)
					startTimeOfOldestRequest = rd.getStartDate();
			}
			totalRequestTime += rd.getTimeTaken();
		}
	}

	private int getWindowSize()
	{
		synchronized (requestWindow)
		{
			if (requestWindow[requestWindow.length - 1] == null)
				return indexInWindow;
			else
				return requestWindow.length;
		}
	}

	public long getAverageRequestTime()
	{
		synchronized (requestWindow)
		{
			int windowSize = getWindowSize();
			if (windowSize == 0)
				return 0;
			return totalRequestTime / windowSize;
		}
	}

	public long getRequestsPerMinute()
	{
		synchronized (requestWindow)
		{
			int windowSize = getWindowSize();
			if (windowSize == 0)
				return 0;
			long start = startTimeOfOldestRequest.getTime();
			long end = System.currentTimeMillis();
			double diff = end - start;
			return Math.round(windowSize / (diff / 60000.0));
		}
	}

	public void logEventTarget(IRequestHandler requestHandler)
	{
		RequestData requestData = getCurrentRequest();
		if (requestData != null)
		{
			requestData.addEventTarget(getRequestHandlerString(requestHandler));
		}
	}

	public void logRequestedUrl(String url)
	{
		getCurrentRequest().setRequestedUrl(url);
	}

	public void logResponseTarget(IRequestHandler requestHandler)
	{
		RequestData requestData = getCurrentRequest();
		if (requestData != null)
			requestData.addResponseTarget(getRequestHandlerString(requestHandler));
	}

	private String getRequestHandlerString(IRequestHandler handler)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(128);
		sb.append(handler.getClass().getSimpleName());
		sb.append("[");
		if (handler instanceof ListenerInterfaceRequestHandler)
		{
			getListenerString(sb, (ListenerInterfaceRequestHandler)handler);
		}
		else if (handler instanceof BookmarkablePageRequestHandler)
		{
			getBookmarkableString(sb, (BookmarkablePageRequestHandler)handler);
		}
		else if (handler instanceof RenderPageRequestHandler)
		{
			getRendererString(sb, (RenderPageRequestHandler)handler);
		}
		else if (handler instanceof AjaxRequestTarget)
		{
			getAjaxString(sb, (AjaxRequestTarget)handler);
		}
		else if (handler instanceof ResourceReferenceRequestHandler)
		{
			getResourceString(sb, (ResourceReferenceRequestHandler)handler);
		}
		else if (handler instanceof IRequestHandlerDelegate)
		{
			getDelegateString(sb, (IRequestHandlerDelegate)handler);
		}
		else if (handler instanceof BufferedResponseRequestHandler)
		{
			// nothing extra to log... BufferedResponse doesn't have identifiable information about
			// which request was buffered
		}
		else
		{
			sb.append(handler.toString());
		}
		sb.append("]");
		return sb.toString();
	}

	private void getDelegateString(AppendingStringBuffer sb, IRequestHandlerDelegate delegateHandler)
	{
		sb.append("delegatedHandler=");
		sb.append(getRequestHandlerString(delegateHandler.getDelegateHandler()));
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

	private void getAjaxString(AppendingStringBuffer sb, AjaxRequestTarget ajaxHandler)
	{
		sb.append("pageClass=");
		sb.append(ajaxHandler.getPageClass().getName());
		sb.append(",pageParameters=[");
		sb.append(ajaxHandler.getPageParameters());
		sb.append("]");
		sb.append(",pageId=");
		sb.append(ajaxHandler.getPage().getId());
	}

	private void getRendererString(AppendingStringBuffer sb,
		RenderPageRequestHandler pageRequestHandler)
	{
		sb.append("pageClass=");
		sb.append(pageRequestHandler.getPageClass().getName());
		sb.append(",pageParameters=[");
		sb.append(pageRequestHandler.getPageParameters());
		sb.append("]");
		IPageProvider pageProvider = pageRequestHandler.getPageProvider();
		if (!pageProvider.isNewPageInstance())
		{
			sb.append(",pageId=");
			sb.append(pageRequestHandler.getPage().getId());
		}
	}

	private void getBookmarkableString(AppendingStringBuffer sb,
		BookmarkablePageRequestHandler pageRequestHandler)
	{
		sb.append("pageClass=");
		sb.append(pageRequestHandler.getPageClass().getName());
		sb.append(",pageParameters=[");
		sb.append(pageRequestHandler.getPageParameters());
		sb.append("]");
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

	/**
	 * Resizes the request buffer to match the
	 * {@link IRequestLoggerSettings#getRequestsWindowSize() configured window size}
	 */
	private void resizeBuffer()
	{
		int newCapacity = Application.get().getRequestLoggerSettings().getRequestsWindowSize();

		// do nothing if the capacity requirement hasn't changed
		if (newCapacity == requestWindow.length)
			return;

		RequestData[] newRequestWindow = new RequestData[newCapacity];
		synchronized (requestWindow)
		{
			int oldCapacity = requestWindow.length;
			int oldNumberOfElements = hasBufferRolledOver() ? oldCapacity : indexInWindow;

			if (newCapacity > oldCapacity)
			{
				// increase the capacity of the buffer when more requests need to be stored
				// and preserve the order of the requests while copying them into the new buffer.
				copyRequestsInOrder(newRequestWindow);

				// the next writable position is at the first non-copied element in the buffer
				indexInWindow = oldNumberOfElements;
				requestWindow = newRequestWindow;
			}
			else if (newCapacity < oldCapacity)
			{
				// sort the requests in the current buffer such that the oldest request is in slot 0
				RequestData[] sortedRequestWindow = new RequestData[oldCapacity];
				copyRequestsInOrder(sortedRequestWindow);

				// determine the number of elements that need to be copied into the smaller target
				int numberOfElementsToCopy = Math.min(newCapacity, oldNumberOfElements);

				// determine the position from where the copying must start
				int numberOfElementsToSkip = Math.max(0, oldNumberOfElements -
					numberOfElementsToCopy);

				// fill the new buffer with the leftovers of the old buffer, skipping the oldest
				// requests
				arraycopy(sortedRequestWindow, numberOfElementsToSkip, newRequestWindow, 0,
					numberOfElementsToCopy);

				// the next writable position is 0 when the buffer is filled to capacity, or the
				// number of copied elements when the buffer isn't filled to capacity.
				indexInWindow = numberOfElementsToCopy >= newCapacity ? 0 : numberOfElementsToCopy;
				requestWindow = newRequestWindow;
			}
		}
	}
}
