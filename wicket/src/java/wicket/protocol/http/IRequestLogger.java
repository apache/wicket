/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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

import java.util.List;

import wicket.Application;
import wicket.IRequestTarget;
import wicket.protocol.http.RequestLogger.RequestData;
import wicket.session.ISessionStore;


/**
 * Interface for the request logger and viewer.
 * @see Application#setRequestLogger(IRequestLogger)
 * 
 * @author jcompagner
 */
public interface IRequestLogger
{

	/**
	 * @return The total created sessions counter
	 */
	public abstract int getTotalCreatedSessions();

	/**
	 * @return The peak sessions counter
	 */
	public abstract int getPeakSessions();

	/**
	 * @return Collection of live Sessions
	 */
	public abstract List<RequestData> getRequests();

	/**
	 * called when the session is created and has an id. 
	 * (for http it means that the http session is created)
	 * 
	 * @param id
	 */
	public abstract void sessionCreated(String id);

	/**
	 * Method used to cleanup a livesession when the session was
	 * invalidated by the webcontainer
	 * 
	 * @param sessionId
	 */
	public abstract void sessionDestroyed(String sessionId);

	/**
	 * This method is called when the request is over this will
	 * set the total time a request takes and cleans up the current 
	 * request data.
	 * 
	 * @param timeTaken
	 */
	public abstract void requestTime(long timeTaken);

	/**
	 * Called to monitor removals of objects out of the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public abstract void objectRemoved(Object value);

	/**
	 * Called to monitor updates of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public abstract void objectUpdated(Object value);

	/**
	 * Called to monitor additions of objects in the {@link ISessionStore}
	 * 
	 * @param value
	 */
	public abstract void objectCreated(Object value);

	/**
	 * Sets the target that was the response target for the current request
	 * 
	 * @param target
	 */
	public abstract void logResponseTarget(IRequestTarget target);

	/**
	 * Sets the target that was the event target for the current request
	 * 
	 * @param target
	 */
	public abstract void logEventTarget(IRequestTarget target);

}