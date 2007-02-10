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
 * Exposes {@link wicket.protocol.http.RequestLogger} for JMX.
 * 
 * @author eelcohillenius
 */
public class RequestLogger implements RequestLoggerMBean
{
	private final wicket.Application application;

	private final WebApplication webApplication;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application
	 */
	public RequestLogger(wicket.Application application)
	{
		this.application = application;

		// do this so that we don't have to cast all the time
		if (application instanceof WebApplication)
		{
			this.webApplication = (WebApplication)application;
		}
		else
		{
			this.webApplication = null;
		}
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#getNumberOfCreatedSessions()
	 */
	public Integer getNumberOfCreatedSessions() throws IOException
	{
		wicket.protocol.http.RequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return Integer.valueOf(logger.getTotalCreatedSessions());
		}
		return null;
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#getNumberOfLiveSessions()
	 */
	public Integer getNumberOfLiveSessions() throws IOException
	{
		wicket.protocol.http.RequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return Integer.valueOf(logger.getLiveSessions().size());
		}
		return null;
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#getPeakNumberOfSessions()
	 */
	public Integer getPeakNumberOfSessions() throws IOException
	{
		wicket.protocol.http.RequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return Integer.valueOf(logger.getPeakSessions());
		}
		return null;
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#restart()
	 */
	public void restart() throws IOException
	{
		if (webApplication != null)
		{
			webApplication.setRequestLogger(new wicket.protocol.http.RequestLogger());
		}
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#stop()
	 */
	public void stop() throws IOException
	{
		if (webApplication != null)
		{
			webApplication.setRequestLogger(null);
		}
	}

	/**
	 * Gets the request logger for this application.
	 * 
	 * @return The request logger or null if no request is active, or if this is
	 *         not a web application
	 */
	protected wicket.protocol.http.RequestLogger getRequestLogger()
	{
		if (application instanceof WebApplication)
		{
			return ((WebApplication)application).getRequestLogger();
		}
		return null;
	}
}
