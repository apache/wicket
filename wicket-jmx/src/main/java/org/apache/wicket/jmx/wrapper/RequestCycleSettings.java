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
package org.apache.wicket.jmx.wrapper;

import org.apache.wicket.Application;
import org.apache.wicket.jmx.RequestCycleSettingsMBean;
import org.apache.wicket.util.time.Duration;


/**
 * Exposes Application related functionality for JMX.
 * 
 * @author eelcohillenius
 */
public class RequestCycleSettings implements RequestCycleSettingsMBean
{
	private final Application application;

	/**
	 * Create.
	 * 
	 * @param application
	 */
	public RequestCycleSettings(final Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#getBufferResponse()
	 */
	@Override
	public boolean getBufferResponse()
	{
		return application.getRequestCycleSettings().getBufferResponse();
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#getGatherExtendedBrowserInfo()
	 */
	@Override
	public boolean getGatherExtendedBrowserInfo()
	{
		return application.getRequestCycleSettings().getGatherExtendedBrowserInfo();
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#getResponseRequestEncoding()
	 */
	@Override
	public String getResponseRequestEncoding()
	{
		return application.getRequestCycleSettings().getResponseRequestEncoding();
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#getTimeout()
	 */
	@Override
	public String getTimeout()
	{
		return application.getRequestCycleSettings().getTimeout().toString();
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#setBufferResponse(boolean)
	 */
	@Override
	public void setBufferResponse(final boolean bufferResponse)
	{
		application.getRequestCycleSettings().setBufferResponse(bufferResponse);
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#setGatherExtendedBrowserInfo(boolean)
	 */
	@Override
	public void setGatherExtendedBrowserInfo(final boolean gatherExtendedBrowserInfo)
	{
		application.getRequestCycleSettings().setGatherExtendedBrowserInfo(
			gatherExtendedBrowserInfo);
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#setResponseRequestEncoding(java.lang.String)
	 */
	@Override
	public void setResponseRequestEncoding(final String responseRequestEncoding)
	{
		application.getRequestCycleSettings().setResponseRequestEncoding(responseRequestEncoding);
	}

	/**
	 * @see org.apache.wicket.jmx.RequestCycleSettingsMBean#setTimeout(java.lang.String)
	 */
	@Override
	public void setTimeout(final String timeout)
	{
		application.getRequestCycleSettings().setTimeout(Duration.valueOf(timeout));
	}

	@Override
	public void setExceptionRetryCount(int retries)
	{
		application.getRequestCycleSettings().setExceptionRetryCount(retries);
	}

	@Override
	public int getExceptionRetryCount()
	{
		return application.getRequestCycleSettings().getExceptionRetryCount();
	}
}
