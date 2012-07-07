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
package org.apache.wicket.jmx;

import java.io.IOException;

import org.apache.wicket.protocol.http.WebApplication;


/**
 * Exposes {@link org.apache.wicket.protocol.http.RequestLogger} for JMX.
 * 
 * @author eelcohillenius
 */
public class RequestLogger implements RequestLoggerMBean
{
	private final org.apache.wicket.Application application;

	private final WebApplication webApplication;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            The application
	 */
	public RequestLogger(final org.apache.wicket.Application application)
	{
		this.application = application;

		// do this so that we don't have to cast all the time
		if (application instanceof WebApplication)
		{
			webApplication = (WebApplication)application;
		}
		else
		{
			webApplication = null;
		}
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#getNumberOfCreatedSessions()
	 */
	@Override
	public Integer getNumberOfCreatedSessions() throws IOException
	{
		org.apache.wicket.protocol.http.IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return logger.getTotalCreatedSessions();
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#getNumberOfLiveSessions()
	 */
	@Override
	public Integer getNumberOfLiveSessions() throws IOException
	{
		org.apache.wicket.protocol.http.IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return logger.getLiveSessions().length;
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#getPeakNumberOfSessions()
	 */
	@Override
	public Integer getPeakNumberOfSessions() throws IOException
	{
		org.apache.wicket.protocol.http.IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return logger.getPeakSessions();
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#getNumberOfCurrentActiveRequests()
	 */
	@Override
	public Integer getNumberOfCurrentActiveRequests() throws IOException
	{
		org.apache.wicket.protocol.http.IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return Integer.valueOf(logger.getCurrentActiveRequestCount());
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#getPeakNumberOfActiveRequests()
	 */
	@Override
	public Integer getPeakNumberOfActiveRequests() throws IOException
	{
		org.apache.wicket.protocol.http.IRequestLogger logger = getRequestLogger();
		if (logger != null)
		{
			return Integer.valueOf(logger.getPeakActiveRequestCount());
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#restart()
	 */
	@Override
	public void restart() throws IOException
	{
		if (webApplication != null)
		{
			webApplication.getRequestLoggerSettings().setRequestLoggerEnabled(false);
			webApplication.getRequestLogger();
			webApplication.getRequestLoggerSettings().setRequestLoggerEnabled(true);
		}
	}

	/**
	 * @see org.apache.wicket.jmx.RequestLoggerMBean#stop()
	 */
	@Override
	public void stop() throws IOException
	{
		if (webApplication != null)
		{
			webApplication.getRequestLoggerSettings().setRequestLoggerEnabled(false);
		}
	}

	/**
	 * Gets the request logger for this application.
	 * 
	 * @return The request logger or null if no request is active, or if this is not a web
	 *         application
	 */
	protected org.apache.wicket.protocol.http.IRequestLogger getRequestLogger()
	{
		if (application instanceof WebApplication)
		{
			return application.getRequestLogger();
		}
		return null;
	}
}
