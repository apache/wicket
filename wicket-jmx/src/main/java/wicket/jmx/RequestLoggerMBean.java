/*
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
package wicket.jmx;

import java.io.IOException;

import wicket.protocol.http.WebApplication;

/**
 * MBean interface for exposing session related information and functionality.
 * 
 * @author eelcohillenius
 */
public interface RequestLoggerMBean
{
	/**
	 * Total number of sessions ever created since the application was started.
	 * <p>
	 * Only available for {@link WebApplication web applications}.
	 * </p>
	 * 
	 * @return the total number of sessions ever created since the application
	 *         was started
	 * @throws IOException
	 */
	Integer getNumberOfCreatedSessions() throws IOException;

	/**
	 * Gets the (recorded) number of currently live sessions.
	 * <p>
	 * Only available for {@link WebApplication web applications}.
	 * </p>
	 * 
	 * @return current (recorded) number of live sessions
	 * @throws IOException
	 */
	Integer getNumberOfLiveSessions() throws IOException;

	/**
	 * The largest number of concurrent sessions since the application was
	 * started.
	 * <p>
	 * Only available for {@link WebApplication web applications}.
	 * </p>
	 * 
	 * @return the largest number of concurrent sessions since the application
	 *         was started
	 * @throws IOException
	 */
	Integer getPeakNumberOfSessions() throws IOException;

	/**
	 * Registers a new request logger at the application. You need a request
	 * logger for some functions of the session bean. Be aware that sessions
	 * will be logged from this time on, so they may not reflect the actual
	 * number of sessions. Also, if one was registered, it will be replaced by a
	 * new one, which then starts over counting, disregarding the current ones.
	 * <p>
	 * Only available for {@link WebApplication web applications}.
	 * </p>
	 * 
	 * @throws IOException
	 */
	void restart() throws IOException;

	/**
	 * Removes any set request logger from the application. You need a request
	 * logger for some functions of the session bean.
	 * <p>
	 * Only available for {@link WebApplication web applications}.
	 * </p>
	 * 
	 * @throws IOException
	 */
	void stop() throws IOException;
}
