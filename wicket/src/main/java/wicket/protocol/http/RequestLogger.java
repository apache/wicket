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
package wicket.protocol.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageMap;
import wicket.Session;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.util.lang.Classes;
import wicket.util.string.AppendingStringBuffer;

/**
 * This is the logger class that can be set in the
 * {@link WebApplication#setRequestLogger(RequestLogger)} method. If this class
 * is set all request and live sessions will be recorded and displayed From the
 * total created sessions, to the peak session count and the current
 * livesessions. For the livesessions the request logger will record what
 * request are happening what kind of {@link IRequestTarget} was the event
 * target and what {@link IRequestTarget} was the response target. It also
 * records what session data was touched for this and how long the request did
 * take.
 * 
 * To view this information live see the {@link InspectorBug} that shows the
 * {@link InspectorPage} with the {@link LiveSessionsPage}
 * 
 * @author jcompagner
 * 
 * @since 1.2
 */
public class RequestLogger implements IRequestLogger
{
	/** log. */
	protected static Logger log = LoggerFactory.getLogger(RequestLogger.class);


	/**
	 * This interface can be implemented in a custom session object. to give an
	 * object that has more information for the current session (state of
	 * session).
	 * 
	 * @author jcompagner
	 */
	public interface ISessionLogInfo
	{

		/**
		 * If you use the request logger log functionality then this object
		 * should have a nice String representation. So make sure that the
		 * toString() is implemented for the returned object.
		 * 
		 * @return The custom object stored in the request loggers current
		 *         request.
		 */
		Object getSessionInfo();
	}


	private int totalCreatedSessions;

	private int peakSessions;

	private List<RequestData> requests;

	private Map<String, SessionData> liveSessions;

	private ThreadLocal<RequestData> currentRequest = new ThreadLocal<RequestData>();

	private AtomicInteger active;

	/**
	 * Construct.
	 */
	public RequestLogger()
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

	/**
	 * @see wicket.protocol.http.IRequestLogger#getTotalCreatedSessions()
	 */
	public int getTotalCreatedSessions()
	{
		return totalCreatedSessions;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#getPeakSessions()
	 */
	public int getPeakSessions()
	{
		return peakSessions;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#getCurrentActiveRequestCount()
	 */
	public int getCurrentActiveRequestCount()
	{
		return active.get();
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#getRequests()
	 */
	public List<RequestData> getRequests()
	{
		return Collections.unmodifiableList(requests);
	}

	public SessionData[] getLiveSessions()
	{
		SessionData[] sessions = liveSessions.values()
				.toArray(new SessionData[liveSessions.size()]);
		Arrays.sort(sessions);
		return sessions;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#sessionDestroyed(java.lang.String)
	 */
	public void sessionDestroyed(String sessionId)
	{
		liveSessions.remove(sessionId);
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#sessionDestroyed(java.lang.String)
	 */
	public void sessionCreated(String sessionId)
	{
		liveSessions.put(sessionId, new SessionData(sessionId));
		if (liveSessions.size() > peakSessions)
			peakSessions = liveSessions.size();
		totalCreatedSessions++;
	}

	RequestData getCurrentRequest()
	{
		RequestData rd = currentRequest.get();
		if (rd == null)
		{
			rd = new RequestData();
			currentRequest.set(rd);
			active.incrementAndGet();
		}
		return rd;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#requestTime(long)
	 */
	public void requestTime(long timeTaken)
	{
		RequestData rd = getCurrentRequest();
		if (rd != null)
		{
			Session session = Session.get();
			String sessionId = session.getId();
			rd.setSessionId(sessionId);

			Object sessionInfo = getSessionInfo(session);
			rd.setSessionInfo(sessionInfo);

			long sizeInBytes = -1;
			if (Application.get().getRequestLoggerSettings().getRecordSessionSize())
			{
				sizeInBytes = session.getSizeInBytes();
			}
			rd.setSessionSize(sizeInBytes);
			rd.setTimeTaken(timeTaken);
			if(active.decrementAndGet() < 0)
			{
				// just a fail save, i don't think this can really happen.
				active.set(0);
			}

			requests.add(0, rd);
			currentRequest.set(null);
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

	/**
	 * @param rd
	 * @param sd
	 */
	private void log(RequestData rd, SessionData sd)
	{
		if (log.isInfoEnabled())
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
			log.info(asb.toString());
		}
	}

	private Object getSessionInfo(Session session)
	{
		if (session instanceof ISessionLogInfo)
		{
			return ((ISessionLogInfo)session).getSessionInfo();
		}
		return "";
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#objectRemoved(java.lang.Object)
	 */
	public void objectRemoved(Object value)
	{
		RequestData rd = getCurrentRequest();
		if (value instanceof Page)
		{
			Page<?> page = (Page<?>)value;
			rd.addEntry("Page removed, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if (value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap removed, name: "
					+ (map.getName() == null ? "DEFAULT" : map.getName()));
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

	/**
	 * @see wicket.protocol.http.IRequestLogger#objectUpdated(java.lang.Object)
	 */
	public void objectUpdated(Object value)
	{
		RequestData rd = getCurrentRequest();
		if (value instanceof Page)
		{
			Page<?> page = (Page<?>)value;
			rd.addEntry("Page updated, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if (value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap updated, name: "
					+ (map.getName() == null ? "DEFAULT" : map.getName()));
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

	/**
	 * @see wicket.protocol.http.IRequestLogger#objectCreated(java.lang.Object)
	 */
	public void objectCreated(Object value)
	{
		RequestData rd = getCurrentRequest();

		if (value instanceof Session)
		{
			rd.addEntry("Session created");
		}
		else if (value instanceof Page)
		{
			Page<?> page = (Page<?>)value;
			rd.addEntry("Page created, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if (value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap created, name: "
					+ (map.getName() == null ? "DEFAULT" : map.getName()));
		}
		else
		{
			rd.addEntry("Custom object created: " + value);
		}

	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#logResponseTarget(wicket.IRequestTarget)
	 */
	public void logResponseTarget(IRequestTarget target)
	{
		getCurrentRequest().addResponseTarget(getRequestTargetString(target));
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#logEventTarget(wicket.IRequestTarget)
	 */
	public void logEventTarget(IRequestTarget target)
	{
		getCurrentRequest().addEventTarget(getRequestTargetString(target));
	}


	/**
	 * @param target
	 * @return The request target nice display string
	 */
	private String getRequestTargetString(IRequestTarget target)
	{
		AppendingStringBuffer sb = new AppendingStringBuffer(128);
		if (target instanceof IListenerInterfaceRequestTarget)
		{
			IListenerInterfaceRequestTarget listener = (IListenerInterfaceRequestTarget)target;
			sb.append("Interface[target:");
			sb.append(Classes.simpleName(listener.getTarget().getClass()));
			sb.append("(");
			sb.append(listener.getTarget().getPageRelativePath());
			sb.append("), page: ");
			sb.append(listener.getPage().getClass().getName());
			sb.append("(");
			sb.append(listener.getPage().getId());
			sb.append("), interface: ");
			sb.append(listener.getRequestListenerInterface().getName());
			sb.append(".");
			sb.append(listener.getRequestListenerInterface().getMethod().getName());
			sb.append("]");
		}
		else if (target instanceof IPageRequestTarget)
		{
			IPageRequestTarget pageRequestTarget = (IPageRequestTarget)target;
			sb.append("PageRequest[");
			sb.append(pageRequestTarget.getPage().getClass().getName());
			sb.append("(");
			sb.append(pageRequestTarget.getPage().getId());
			sb.append(")]");
		}
		else if (target instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget pageRequestTarget = (IBookmarkablePageRequestTarget)target;
			sb.append("BookmarkablePage[");
			sb.append(pageRequestTarget.getPageClass().getName());
			sb.append("]");
		}
		else if (target instanceof ISharedResourceRequestTarget)
		{
			ISharedResourceRequestTarget sharedResourceTarget = (ISharedResourceRequestTarget)target;
			sb.append("SharedResource[");
			sb.append(sharedResourceTarget.getResourceKey());
			sb.append("]");
		}
		else
		{
			sb.append(target.toString());
		}
		return sb.toString();
	}

	/**
	 * This class hold the information one request of a session has.
	 * 
	 * @author jcompagner
	 */
	public static class SessionData implements Serializable, Comparable<SessionData>
	{
		private static final long serialVersionUID = 1L;

		private String sessionId;
		private long startDate;
		private long lastActive;
		private long numberOfRequests;
		private long totalTimeTaken;
		private long sessionSize;
		private Object sessionInfo;

		/**
		 * Construct.
		 * 
		 * @param sessionId
		 */
		public SessionData(String sessionId)
		{
			this.sessionId = sessionId;
			this.startDate = System.currentTimeMillis();
			this.numberOfRequests = 1;
		}

		/**
		 * @return The last active date.
		 */
		public Date getLastActive()
		{
			return new Date(lastActive);
		}

		/**
		 * @return The start date of this session
		 */
		public Date getStartDate()
		{
			return new Date(startDate);
		}

		/**
		 * @return The number of request for this session
		 */
		public long getNumberOfRequests()
		{
			return numberOfRequests;
		}

		/**
		 * @return Returns the session size.
		 */
		public long getSessionSize()
		{
			return sessionSize;
		}

		/**
		 * @return Returns the total time this session has spent.
		 */
		public long getTotalTimeTaken()
		{
			return totalTimeTaken;
		}

		/**
		 * @return The session info object given by the
		 *         {@link ISessionLogInfo#getSessionInfo()} session method.
		 */
		public Object getSessionInfo()
		{
			return sessionInfo;
		}

		/**
		 * @return The session id
		 */
		public String getSessionId()
		{
			return sessionId;
		}

		void addTimeTaken(long time)
		{
			this.lastActive = System.currentTimeMillis();
			this.numberOfRequests++;
			this.totalTimeTaken += time;
		}

		void setSessionInfo(Object sessionInfo)
		{
			this.sessionInfo = sessionInfo;
		}

		void setSessionSize(long size)
		{
			this.sessionSize = size;
		}

		public int compareTo(SessionData sd)
		{
			return (int)(sd.lastActive - lastActive);
		}

	}

	/**
	 * This class hold the information one request of a session has.
	 * 
	 * @author jcompagner
	 */
	public static class RequestData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private long startDate;
		private long timeTaken;
		private List<String> entries = new ArrayList<String>(5);
		private String eventTarget;
		private String responseTarget;

		private String sessionId;

		private long totalSessionSize;

		private Object sessionInfo;

		private int activeRequest;

		/**
		 * @return The time taken for this request
		 */
		public Long getTimeTaken()
		{
			return new Long(timeTaken);
		}

		/**
		 * @param activeRequest
		 *            The number of active request when this request happend
		 */
		public void setActiveRequest(int activeRequest)
		{
			this.activeRequest = activeRequest;
		}

		/**
		 * @return The number of active request when this request happend
		 */
		public int getActiveRequest()
		{
			return activeRequest;
		}

		/**
		 * @return The session object info, created by
		 *         {@link ISessionLogInfo#getSessionInfo()}
		 */
		public Object getSessionInfo()
		{
			return sessionInfo;
		}

		/**
		 * Set the session info object of the session for this request.
		 * 
		 * @param sessionInfo
		 */
		public void setSessionInfo(Object sessionInfo)
		{
			this.sessionInfo = sessionInfo;
		}

		/**
		 * @param sizeInBytes
		 */
		public void setSessionSize(long sizeInBytes)
		{
			totalSessionSize = sizeInBytes;
		}

		/**
		 * @param id
		 */
		public void setSessionId(String id)
		{
			sessionId = id;
		}

		/**
		 * @return The time taken for this request
		 */
		public Date getStartDate()
		{
			return new Date(startDate);
		}

		/**
		 * @return The event target string
		 */
		public String getEventTarget()
		{
			return eventTarget;
		}

		/**
		 * @return The response target string
		 */
		public String getResponseTarget()
		{
			return responseTarget;
		}

		/**
		 * @param target
		 */
		public void addResponseTarget(String target)
		{
			this.responseTarget = target;
		}

		/**
		 * @param target
		 */
		public void addEventTarget(String target)
		{
			this.eventTarget = target;
		}

		/**
		 * @param timeTaken
		 */
		public void setTimeTaken(long timeTaken)
		{
			this.timeTaken = timeTaken;
			this.startDate = System.currentTimeMillis() - timeTaken;
		}

		/**
		 * @param string
		 */
		public void addEntry(String string)
		{
			entries.add(string);
		}

		/**
		 * @return All entries of the objects that are created/updated or
		 *         removed in this request
		 */
		public String getAlteredObjects()
		{
			AppendingStringBuffer sb = new AppendingStringBuffer();
			for (int i = 0; i < entries.size(); i++)
			{
				String element = entries.get(i);
				sb.append(element);
				if (entries.size() != i + 1)
				{
					sb.append("<br/>");
				}
			}
			return sb.toString();
		}

		/**
		 * @return The session id for this request
		 */
		public String getSessionId()
		{
			return sessionId;
		}

		/**
		 * @return The total session size.
		 */
		public Long getSessionSize()
		{
			return totalSessionSize;
		}

		@Override
		public String toString()
		{
			return "Request[timetaken=" + getTimeTaken() + ",sessioninfo=" + sessionInfo
					+ ",sessionid=" + sessionId + ",sessionsize=" + totalSessionSize + ",request="
					+ eventTarget + ",response=" + responseTarget + ",alteredobjects="
					+ getAlteredObjects() + ",activerequest=" + activeRequest + "]";
		}
	}
}
