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
package wicket.jmx;

import java.io.IOException;

import wicket.protocol.http.IRequestLogger;
import wicket.protocol.http.RequestLogger.SessionData;

/**
 * Exposes {@link wicket.protocol.http.RequestLogger} for JMX.
 * 
 * @author eelcohillenius
 */
public class RequestLogger implements RequestLoggerMBean
{
	private final wicket.Application application;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application
	 */
	public RequestLogger(wicket.Application application)
	{
		this.application = application;
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#getNumberOfCreatedSessions()
	 */
	public Integer getNumberOfCreatedSessions() throws IOException
	{
		IRequestLogger logger = getRequestLogger();
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
		IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			SessionData[] liveSessions = logger.getLiveSessions();
			return (liveSessions != null) ? Integer.valueOf(liveSessions.length) : Integer
					.valueOf(0);
		}
		return null;
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#getPeakNumberOfSessions()
	 */
	public Integer getPeakNumberOfSessions() throws IOException
	{
		IRequestLogger logger = getRequestLogger();
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
		application.getRequestLoggerSettings().setRequestLoggerEnabled(false);
		application.getRequestLogger();
		application.getRequestLoggerSettings().setRequestLoggerEnabled(true);
	}

	/**
	 * @see wicket.jmx.RequestLoggerMBean#stop()
	 */
	public void stop() throws IOException
	{
		application.getRequestLoggerSettings().setRequestLoggerEnabled(false);
	}

	/**
	 * Gets the request logger for this application.
	 * 
	 * @return The request logger or null if no request is active, or if this is
	 *         not a web application
	 */
	protected IRequestLogger getRequestLogger()
	{
		return application.getRequestLogger();
	}
}
