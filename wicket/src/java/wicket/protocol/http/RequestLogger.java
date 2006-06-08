/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wicket.IRequestTarget;
import wicket.Page;
import wicket.PageMap;
import wicket.Session;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.request.target.component.IPageRequestTarget;
import wicket.request.target.component.listener.IListenerInterfaceRequestTarget;
import wicket.request.target.resource.ISharedResourceRequestTarget;
import wicket.session.ISessionStore;
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
 * This class is still a bit experimental for the 1.2 release. Will improve
 * further in 2.0
 * 
 * @author jcompagner
 * 
 * @since 1.2
 */
public class RequestLogger
{
	// TODO post 1.2 for this class: saving to a log file, only holding a small
	// part in mem.


	private int totalCreatedSessions;

	private int peakSessions;

	private Map<String, SessionData> liveSessions;

	/**
	 * Construct.
	 */
	public RequestLogger()
	{
		liveSessions = new ConcurrentHashMap<String, SessionData>();
	}

	/**
	 * @return The total created sessions counter
	 */
	public int getTotalCreatedSessions()
	{
		return totalCreatedSessions;
	}

	/**
	 * @return The peak sessions counter
	 */
	public int getPeakSessions()
	{
		return peakSessions;
	}

	/**
	 * @return Collection of live Sessions
	 */
	public Collection<SessionData> getLiveSessions()
	{
		return liveSessions.values();
	}

	/**
	 * Method used to cleanup a livesession when the session was invalidated by
	 * the webcontainer
	 * 
	 * @param sessionId
	 */
	public void sessionDestroyed(String sessionId)
	{
		liveSessions.remove(sessionId);
	}

	/**
	 * This method is called when the request is over this will set the total
	 * time a request takes and cleans up the current request data.
	 * 
	 * @param timeTaken
	 */
	public void requestTime(long timeTaken)
	{
		SessionData sd = getSessionData();
		sd.endRequest(timeTaken);
	}

	/**
	 * Called to monitor removals of objects out of the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public void objectRemoved(Object value)
	{
		SessionData sd = getSessionData();
		if (value instanceof Page)
		{
			sd.pageRemoved((Page)value);
		}
		else if (value instanceof PageMap)
		{
			sd.pageMapRemoved((PageMap)value);
		}
		else if (value instanceof WebSession)
		{
			sd.webSessionRemoved((WebSession)value);
		}
		else
		{
			// unknown object/custom object?
		}
	}

	/**
	 * Called to monitor updates of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public void objectUpdated(Object value)
	{
		SessionData sd = getSessionData();
		if (value instanceof Page)
		{
			sd.pageUpdated((Page)value);
		}
		else if (value instanceof PageMap)
		{
			sd.pageMapUpdated((PageMap)value);
		}
		else if (value instanceof WebSession)
		{
			sd.webSessionUpdated((WebSession)value);
		}
		else
		{
			// unknown object/custom object?
		}

	}

	/**
	 * Called to monitor additions of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public void objectCreated(Object value)
	{
		SessionData sd = null;
		// Special case, if session is created then getSessionData()
		// can't be called because Session.get() does fail. and there is no
		// SessionData anyway so directly create one.
		if (value instanceof Session)
		{
			sd = createSessionData((Session)value);
		}
		else
		{
			sd = getSessionData();
		}
		if (value instanceof Page)
		{
			sd.pageCreated((Page)value);
		}
		else if (value instanceof PageMap)
		{
			sd.pageMapCreated((PageMap)value);
		}
		else if (value instanceof WebSession)
		{
			sd.webSessionCreated((WebSession)value);
		}
		else
		{
			// unknown object/custom object?
		}

	}

	/**
	 * Sets the target that was the response target for the current request
	 * 
	 * @param target
	 */
	public void logResponseTarget(IRequestTarget target)
	{
		getSessionData().logResponseTarget(target);
	}

	/**
	 * Sets the target that was the event target for the current request
	 * 
	 * @param target
	 */
	public void logEventTarget(IRequestTarget target)
	{
		getSessionData().logEventTarget(target);
	}

	private SessionData getSessionData()
	{
		Session session = Session.get();
		String sessionId = session.getId();

		SessionData sessionData = (sessionId != null) ? liveSessions.get(sessionId) : null;
		if (sessionData == null)
		{
			sessionData = createSessionData(session);
		}
		return sessionData;
	}

	/**
	 * @param session
	 * @return The SessionData object
	 */
	private SessionData createSessionData(Session session)
	{
		SessionData sessionData = new SessionData(session);
		String sessionId = session.getId();
		if (sessionId != null)
		{
			liveSessions.put(sessionId, sessionData);
			totalCreatedSessions++;
			if (peakSessions < liveSessions.size())
			{
				peakSessions = liveSessions.size();
			}
		}
		return sessionData;
	}

	/**
	 * This class hols the information one sessions has
	 * 
	 * @author jcompagner
	 */
	public static class SessionData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Session session;

		private LinkedList<RequestData> requests;

		private RequestData currentRequest;

		private double totalRequestsTime;

		/**
		 * Construct.
		 * 
		 * @param session
		 */
		public SessionData(Session session)
		{
			this.session = session;
			this.requests = new LinkedList<RequestData>();
		}

		/**
		 * @return The session id
		 */
		public String getId()
		{
			return session.getId();
		}

		/**
		 * @return The session
		 */
		public Session getSession()
		{
			return session;
		}


		/**
		 * @return The request list of this session
		 */
		public List<RequestData> getRequests()
		{
			return requests;
		}

		/**
		 * @return The total session size
		 */
		public long getSessionSize()
		{
			return session.getSizeInBytes();
		}

		/**
		 * @return The total time in seconds all request did take
		 */
		public Double getRequestsTime()
		{
			return new Double(totalRequestsTime / 1000);
		}

		/**
		 * @param target
		 */
		public void logEventTarget(IRequestTarget target)
		{
			getCurrentRequest().addEventTarget(getRequestTargetString(target));
		}

		/**
		 * @param target
		 */
		public void logResponseTarget(IRequestTarget target)
		{
			getCurrentRequest().addResponseTarget(getRequestTargetString(target));
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
				sb.append("Interface call [target:");
				sb.append(Classes.simpleName(listener.getTarget().getClass()));
				sb.append("(");
				sb.append(listener.getTarget().getId());
				sb.append("), page: ");
				sb.append(Classes.simpleName(listener.getPage().getClass()));
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
				sb.append("PageRequest call [page: ");
				sb.append(Classes.simpleName(pageRequestTarget.getPage().getClass()));
				sb.append("(");
				sb.append(pageRequestTarget.getPage().getId());
				sb.append(")]");
			}
			else if (target instanceof IBookmarkablePageRequestTarget)
			{
				IBookmarkablePageRequestTarget pageRequestTarget = (IBookmarkablePageRequestTarget)target;
				sb.append("BookmarkablePage call [page: ");
				sb.append(Classes.simpleName(pageRequestTarget.getPageClass()));
				sb.append("]");
			}
			else if (target instanceof ISharedResourceRequestTarget)
			{
				ISharedResourceRequestTarget sharedResourceTarget = (ISharedResourceRequestTarget)target;
				sb.append("Shared Resource call [resourcekey: ");
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
		 * @param page
		 */
		public void pageCreated(Page page)
		{
			getCurrentRequest().addEntry(
					"Page created, id: " + page.getId() + ", class:" + page.getClass());
		}

		/**
		 * @param map
		 */
		public void pageMapCreated(PageMap map)
		{
			getCurrentRequest()
					.addEntry(
							"PageMap created, name: "
									+ (map.getName() == null ? "DEFAULT" : map.getName()));
		}

		/**
		 * @param session
		 */
		public void webSessionCreated(WebSession session)
		{
			getCurrentRequest().addEntry("WebSession created");
		}

		/**
		 * @param session
		 */
		public void webSessionUpdated(WebSession session)
		{
			getCurrentRequest().addEntry("WebSession updated");
		}

		/**
		 * @param map
		 */
		public void pageMapUpdated(PageMap map)
		{
			getCurrentRequest()
					.addEntry(
							"PageMap updated, name: "
									+ (map.getName() == null ? "DEFAULT" : map.getName()));
		}

		/**
		 * @param page
		 */
		public void pageUpdated(Page page)
		{
			getCurrentRequest().addEntry(
					"Page updated, id: " + page.getId() + ", class:" + page.getClass());
		}

		/**
		 * @param session
		 */
		public void webSessionRemoved(WebSession session)
		{
			getCurrentRequest().addEntry("WebSession removed");
		}

		/**
		 * @param map
		 */
		public void pageMapRemoved(PageMap map)
		{
			getCurrentRequest()
					.addEntry(
							"PageMap removed, name: "
									+ (map.getName() == null ? "DEFAULT" : map.getName()));
		}

		/**
		 * @param page
		 */
		public void pageRemoved(Page page)
		{
			getCurrentRequest().addEntry(
					"Page removed, id: " + page.getId() + ", class:" + page.getClass());
		}

		/**
		 * @param timeTaken
		 */
		public void endRequest(long timeTaken)
		{
			RequestData rd = getCurrentRequest();
			rd.setTimeTaken(timeTaken);
			totalRequestsTime += timeTaken;
			currentRequest = null;
		}

		private RequestData getCurrentRequest()
		{
			if (currentRequest == null)
			{
				currentRequest = new RequestData();
				requests.addFirst(currentRequest);
				if (requests.size() > 1000)
				{
					requests.removeLast();
				}
			}
			return currentRequest;
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

		private Date startDate;
		private long timeTaken;
		private List<String> entries = new ArrayList<String>(5);
		private String eventTarget;
		private String responseTarget;

		/**
		 * @return The time taken for this request
		 */
		public Long getTimeTaken()
		{
			return new Long(timeTaken);
		}

		/**
		 * @return The time taken for this request
		 */
		public Date getStartDate()
		{
			return startDate;
		}

		/**
		 * @return The event target string
		 */
		public String getEventTargert()
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
			this.startDate = new Date(System.currentTimeMillis() - timeTaken);
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
				if (entries.size() != i - 1)
				{
					sb.append("<br/>");
				}
			}
			return sb.toString();
		}

	}
}
