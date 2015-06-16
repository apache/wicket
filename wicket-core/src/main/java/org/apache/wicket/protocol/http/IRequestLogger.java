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

import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.logger.NoLogData;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.string.Strings;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for the request logger and viewer.
 * 
 * @see org.apache.wicket.Application#newRequestLogger()
 * 
 * @author jcompagner
 */
public interface IRequestLogger
{
	/**
	 * @return The total created sessions counter
	 */
	int getTotalCreatedSessions();

	/**
	 * @return The peak sessions counter
	 */
	int getPeakSessions();

	/**
	 * This method returns a List of the current requests that are in mem. This is a readonly list.
	 * 
	 * @return Collection of the current requests
	 */
	List<RequestData> getRequests();

	/**
	 * @return Collection of live Sessions Data
	 */
	SessionData[] getLiveSessions();

	/**
	 * @return The current active requests
	 */
	int getCurrentActiveRequestCount();

	/**
	 * @return The {@link org.apache.wicket.protocol.http.IRequestLogger.RequestData} for the current request.
	 */
	RequestData getCurrentRequest();

	/**
	 * @return The peak active requests
	 */
	int getPeakActiveRequestCount();

	/**
	 * @return The number of requests per minute.
	 */
	long getRequestsPerMinute();

	/**
	 * @return The average request time.
	 */
	long getAverageRequestTime();

	/**
	 * called when the session is created and has an id. (for http it means that the http session is
	 * created)
	 * 
	 * @param id
	 *            the session id
	 */
	void sessionCreated(String id);

	/**
	 * Method used to cleanup a livesession when the session was invalidated by the webcontainer
	 * 
	 * @param sessionId
	 *            the session id
	 */
	void sessionDestroyed(String sessionId);

	/**
	 * This method is called when the request is over. This will set the total time a request takes
	 * and cleans up the current request data.
	 * 
	 * @param timeTaken
	 *            the time taken in milliseconds
	 */
	void requestTime(long timeTaken);

	/**
	 * Called to monitor removals of objects out of the {@link ISessionStore}
	 * 
	 * @param value
	 *            the object being removed
	 */
	void objectRemoved(Object value);

	/**
	 * Called to monitor updates of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 *            the object being updated
	 */
	void objectUpdated(Object value);

	/**
	 * Called to monitor additions of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 *            the object being created/added
	 */
	void objectCreated(Object value);

	/**
	 * Sets the target that was the response target for the current request
	 * 
	 * @param target
	 *            the response target
	 */
	void logResponseTarget(IRequestHandler target);

	/**
	 * Sets the target that was the event target for the current request
	 * 
	 * @param target
	 *            the event target
	 */
	void logEventTarget(IRequestHandler target);

	/**
	 * Logs the URL that was requested by the browser.
	 * 
	 * @param url
	 *            the requested URL
	 */
	void logRequestedUrl(String url);

	/**
	 * Perform the actual logging
	 */
	public void performLogging();

	/**
	 * This class hold the information one request of a session has.
	 * 
	 * @author jcompagner
	 */
	public static class SessionData implements IClusterable, Comparable<SessionData>
	{
		private static final long serialVersionUID = 1L;

		private final String sessionId;
		private final long startDate;
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
			startDate = System.currentTimeMillis();
			numberOfRequests = 1;
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
		 * @return Returns the total time this session has spent in ms.
		 */
		public long getTotalTimeTaken()
		{
			return totalTimeTaken;
		}

		/**
		 * @return The session info object given by the {@link ISessionLogInfo#getSessionInfo()}
		 *         session method.
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

		/**
		 * Adds {@code time} to the total server time.
		 * 
		 * @param time
		 */
		public void addTimeTaken(long time)
		{
			lastActive = System.currentTimeMillis();
			numberOfRequests++;
			totalTimeTaken += time;
		}

		/**
		 * Sets additional session info (e.g. logged in user).
		 * 
		 * @param sessionInfo
		 */
		public void setSessionInfo(Object sessionInfo)
		{
			this.sessionInfo = sessionInfo;
		}

		/**
		 * Sets the recorded session size.
		 * 
		 * @param size
		 */
		public void setSessionSize(long size)
		{
			sessionSize = size;
		}

		@Override
		public int compareTo(SessionData sd)
		{
			if (sd.startDate > startDate)
			{
				return 1;
			}
			else if (sd.startDate < startDate)
			{
				return -1;
			}
			return 0;
		}
	}


	/**
	 * This class hold the information one request of a session has.
	 * 
	 * @author jcompagner
	 */
	public static class RequestData implements IClusterable
	{
		private static final long serialVersionUID = 1L;

		private long startDate;
		private long timeTaken;
		private final List<String> entries = new ArrayList<>(5);
		private Map<String, Object> userData;
		private String requestedUrl;
		private IRequestHandler eventTarget;
		private IRequestHandler responseTarget;
		private String sessionId;
		private long totalSessionSize;
		private Object sessionInfo;
		private int activeRequest;

		/**
		 * @return The time taken for this request
		 */
		public Long getTimeTaken()
		{
			return timeTaken;
		}

		/**
		 * @param activeRequest
		 *            The number of active request when this request happened
		 */
		public void setActiveRequest(int activeRequest)
		{
			this.activeRequest = activeRequest;
		}

		/**
		 * @return The number of active request when this request happened
		 */
		public int getActiveRequest()
		{
			return activeRequest;
		}

		/**
		 * @return The session object info, created by {@link ISessionLogInfo#getSessionInfo()}
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
		 * @return The event target
		 */
		public IRequestHandler getEventTarget()
		{
			return eventTarget;
		}

		/**
		 * @return The class of the event target
		 */
		public Class<? extends IRequestHandler> getEventTargetClass()
		{
			return eventTarget == null ? null : eventTarget.getClass();
		}

		/**
		 * @return The log data for the eventTarget, or {@link NoLogData} if the request handler is
		 *         not loggable
		 */
		public ILogData getEventTargetLog()
		{
			if (eventTarget instanceof ILoggableRequestHandler)
				return ((ILoggableRequestHandler)eventTarget).getLogData();
			return new NoLogData();
		}

		/**
		 * @return The response target
		 */
		public IRequestHandler getResponseTarget()
		{
			return responseTarget;
		}

		/**
		 * @return The class of the response target
		 */
		public Class<? extends IRequestHandler> getResponseTargetClass()
		{
			return responseTarget == null ? null : responseTarget.getClass();
		}

		/**
		 * @return The log data for the responseTarget, or {@link NoLogData} if the request handler
		 *         is not loggable
		 */
		public ILogData getResponseTargetLog()
		{
			if (responseTarget instanceof ILoggableRequestHandler)
				return ((ILoggableRequestHandler)responseTarget).getLogData();
			return new NoLogData();
		}

		/**
		 * @return the requested URL by the browser
		 */
		public String getRequestedUrl()
		{
			return requestedUrl;
		}

		/**
		 * @param requestedUrl
		 */
		public void setRequestedUrl(String requestedUrl)
		{
			this.requestedUrl = requestedUrl;
		}

		/**
		 * @param target
		 */
		public void setResponseTarget(IRequestHandler target)
		{
			responseTarget = target;
		}

		/**
		 * @param target
		 */
		public void setEventTarget(IRequestHandler target)
		{
			eventTarget = target;
		}

		/**
		 * @param timeTaken
		 */
		public void setTimeTaken(long timeTaken)
		{
			this.timeTaken = timeTaken;
			startDate = System.currentTimeMillis() - timeTaken;
		}

		/**
		 * @param string
		 */
		public void addEntry(String string)
		{
			entries.add(string);
		}

		/**
		 * @param key
		 * @param value
		 */
		public void addUserData(String key, Object value)
		{
			getUserData().put(key, value);
		}

		/**
		 * @param key
		 * @return
		 */
		public Object getUserData(String key)
		{
			return getUserData().get(key);
		}

		/**
		 * @return the userData Map
		 */
		public Map<String, Object> getUserData()
		{
			if (userData == null) {
				userData = new HashMap<>();
			}

			return userData;
		}

		/**
		 * @return All entries of the objects that are created/updated or removed in this request
		 */
		public String getAlteredObjects()
		{
			return Strings.join(", ", entries);
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
			return "Request[timetaken=" + getTimeTaken() + ",sessioninfo=" + sessionInfo +
				",sessionid=" + sessionId + ",sessionsize=" + totalSessionSize + ",request=" +
				eventTarget + ",response=" + responseTarget + ",alteredobjects=" +
				getAlteredObjects() + ",activerequest=" + activeRequest + "]";
		}
	}

	/**
	 * This interface can be implemented in a custom session object. to give an object that has more
	 * information for the current session (state of session).
	 * 
	 * @author jcompagner
	 */
	public interface ISessionLogInfo
	{
		/**
		 * If you use the request logger log functionality then this object should have a nice
		 * String representation. So make sure that the toString() is implemented for the returned
		 * object.
		 * 
		 * @return The custom object stored in the request loggers current request.
		 */
		Object getSessionInfo();
	}
}
