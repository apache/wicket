/*
 * $Id$
 * $Revision$
 * $Date$
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
 * $Id$
 * $Revision$
 * $Date$
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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
 * This is the logger class that can be set in the {@link WebApplication#setRequestLogger(RequestLogger)}
 * method. If this class is set all request and live sessions will be recorded and displayed
 * From the total created sessions, to the peak session count and the current livesessions.
 * For the livesessions the request logger will record what request are happening
 * what kind of {@link IRequestTarget} was the event target and what {@link IRequestTarget}
 * was the response target. It also records what session data was touched for this and
 * how long the request did take.
 * 
 * To view this information live see the {@link InspectorBug} that shows the {@link InspectorPage}
 * with the {@link LiveSessionsPage}
 * 
 * This class is still a bit experimental for the 1.2 release. Will improve further in 2.0
 * 
 * @author jcompagner
 * 
 * @since 1.2
 */
public class RequestLogger implements IRequestLogger
{
	// TODO post 1.2 for this class: saving to a log file, only holding a small part in mem.
	
	
	private int totalCreatedSessions;
	
	private int peakSessions;
	
	private List<RequestData> requests;

	private int liveSessions;

	private ThreadLocal<RequestData> currentRequest = new ThreadLocal<RequestData>();
	
	/**
	 * Construct.
	 */
	public RequestLogger()
	{
		requests = Collections.synchronizedList(new LinkedList<RequestData>());
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
	 * @see wicket.protocol.http.IRequestLogger#getRequests()
	 */
	public List<RequestData> getRequests()
	{
		return requests;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#sessionDestroyed(java.lang.String)
	 */
	public void sessionDestroyed(String sessionId)
	{
		liveSessions--;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#sessionDestroyed(java.lang.String)
	 */
	public void sessionCreated(String sessionId)
	{
		liveSessions++;
		totalCreatedSessions++;
	}
	
	RequestData getCurrentRequest()
	{
		RequestData rd = currentRequest.get();
		if(rd == null)
		{
			rd = new RequestData();
			currentRequest.set(rd);
		}
		return rd;
	}

	/**
	 * @see wicket.protocol.http.IRequestLogger#requestTime(long)
	 */
	public void requestTime(long timeTaken)
	{
		RequestData rd = currentRequest.get();
		if(rd != null)
		{
			Session session = Session.get();
			rd.setSessionId(session.getId());
			// todo should we really do this, this is a bit expensive.
			rd.setSessionSize(session.getSizeInBytes());
			rd.setTimeTaken(timeTaken);
			requests.add(0, rd);
			currentRequest.set(null);
		}
	}
	
	/**
	 * @see wicket.protocol.http.IRequestLogger#objectRemoved(java.lang.Object)
	 */
	public void objectRemoved(Object value)
	{
		RequestData rd = getCurrentRequest();
		if(value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page removed, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if(value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap removed, name: " + (map.getName()==null?"DEFAULT":map.getName()));
		}
		else if(value instanceof WebSession)
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
		if(value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page updated, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if(value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap updated, name: " + (map.getName()==null?"DEFAULT":map.getName()));
		}
		else if(value instanceof Session)
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
		
		if( value instanceof Session )
		{
			rd.addEntry("Session created"); 
		}
		else if(value instanceof Page)
		{
			Page page = (Page)value;
			rd.addEntry("Page created, id: " + page.getId() + ", class:" + page.getClass());
		}
		else if(value instanceof PageMap)
		{
			PageMap map = (PageMap)value;
			rd.addEntry("PageMap created, name: " + (map.getName()==null?"DEFAULT":map.getName()));
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
		if(target instanceof IListenerInterfaceRequestTarget)
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
		else if(target instanceof IPageRequestTarget)
		{
			IPageRequestTarget pageRequestTarget = (IPageRequestTarget)target;
			sb.append("PageRequest call [page: ");
			sb.append(Classes.simpleName(pageRequestTarget.getPage().getClass()));
			sb.append("(");
			sb.append(pageRequestTarget.getPage().getId());
			sb.append(")]");
		}
		else if(target instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget pageRequestTarget = (IBookmarkablePageRequestTarget)target;
			sb.append("BookmarkablePage call [page: ");
			sb.append(Classes.simpleName(pageRequestTarget.getPageClass()));
			sb.append("]");
		}
		else if(target instanceof ISharedResourceRequestTarget)
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

		private String sessionId;

		private long totalSessionSize;
		
		/**
		 * @return The time taken for this request
		 */
		public Long getTimeTaken()
		{
			return new Long(timeTaken);
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
			this.startDate = new Date(System.currentTimeMillis()-timeTaken);
		}

		/**
		 * @param string
		 */
		public void addEntry(String string)
		{
			entries.add(string);
		}

		/**
		 * @return All entries of the objects that are created/updated or removed in this request
		 */
		public String getAlteredObjects()
		{
			AppendingStringBuffer sb = new AppendingStringBuffer();
			for (int i = 0; i < entries.size(); i++)
			{
				String element = entries.get(i);
				sb.append(element);
				if(entries.size() != i-1)
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
		
	}
}
