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
package org.apache.wicket.settings.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Duration;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class RequestCycleSettings implements IRequestCycleSettings
{
	/** True if the response should be buffered */
	private boolean bufferResponse = true;

	/**
	 * Whether Wicket should try to get extensive client info by redirecting to
	 * {@link org.apache.wicket.markup.html.pages.BrowserInfoPage a page that polls for client
	 * capabilities}. False by default.
	 */
	private boolean gatherExtendedBrowserInfo = false;

	/** Type of handling for unexpected exceptions */
	private IExceptionSettings.UnexpectedExceptionDisplay unexpectedExceptionDisplay = IExceptionSettings.SHOW_EXCEPTION_PAGE;

	/**
	 * The render strategy, defaults to 'REDIRECT_TO_BUFFER'. This property influences the default
	 * way in how a logical request that consists of an 'action' and a 'render' part is handled, and
	 * is mainly used to have a means to circumvent the 'refresh' problem.
	 */
	private IRequestCycleSettings.RenderStrategy renderStrategy = RenderStrategy.REDIRECT_TO_BUFFER;

	/** List of {@link org.apache.wicket.IResponseFilter}s. */
	private List<IResponseFilter> responseFilters;

	/**
	 * In order to do proper form parameter decoding it is important that the response and the
	 * following request have the same encoding. see
	 * http://www.crazysquirrel.com/computing/general/form-encoding.jspx for additional information.
	 */
	private String responseRequestEncoding = "UTF-8";

	/**
	 * The time that a request will by default be waiting for the previous request to be handled
	 * before giving up. Defaults to one minute.
	 */
	private Duration timeout = Duration.ONE_MINUTE;

// ****************************************************************************
// IRequestCycleSettings Implementation
// ****************************************************************************

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#addResponseFilter(org.apache.wicket.IResponseFilter)
	 */
	public void addResponseFilter(IResponseFilter responseFilter)
	{
		if (responseFilters == null)
		{
			responseFilters = new ArrayList<IResponseFilter>(3);
		}
		responseFilters.add(responseFilter);
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getBufferResponse()
	 */
	public boolean getBufferResponse()
	{
		return bufferResponse;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getGatherExtendedBrowserInfo()
	 */
	public boolean getGatherExtendedBrowserInfo()
	{
		return gatherExtendedBrowserInfo;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getRenderStrategy()
	 */
	public IRequestCycleSettings.RenderStrategy getRenderStrategy()
	{
		return renderStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getResponseFilters()
	 */
	public List<IResponseFilter> getResponseFilters()
	{
		if (responseFilters == null)
		{
			return null;
		}
		else
		{
			return Collections.unmodifiableList(responseFilters);
		}
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getResponseRequestEncoding()
	 */
	public String getResponseRequestEncoding()
	{
		return responseRequestEncoding;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getTimeout()
	 */
	public Duration getTimeout()
	{
		return timeout;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#getUnexpectedExceptionDisplay()
	 */
	public IExceptionSettings.UnexpectedExceptionDisplay getUnexpectedExceptionDisplay()
	{
		return unexpectedExceptionDisplay;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setBufferResponse(boolean)
	 */
	public void setBufferResponse(boolean bufferResponse)
	{
		this.bufferResponse = bufferResponse;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setGatherExtendedBrowserInfo(boolean)
	 */
	public void setGatherExtendedBrowserInfo(boolean gatherExtendedBrowserInfo)
	{
		this.gatherExtendedBrowserInfo = gatherExtendedBrowserInfo;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setRenderStrategy(org.apache.wicket.settings.Settings.RenderStrategy)
	 */
	public void setRenderStrategy(IRequestCycleSettings.RenderStrategy renderStrategy)
	{
		this.renderStrategy = renderStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setResponseRequestEncoding(java.lang.String)
	 */
	public void setResponseRequestEncoding(final String encoding)
	{
		Args.notNull(encoding, "encoding");
		this.responseRequestEncoding = encoding;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setTimeout(org.apache.wicket.util.time.Duration)
	 */
	public void setTimeout(Duration timeout)
	{
		if (timeout == null)
		{
			throw new IllegalArgumentException("timeout cannot be null");
		}
		this.timeout = timeout;
	}

	/**
	 * @see org.apache.wicket.settings.IRequestCycleSettings#setUnexpectedExceptionDisplay(org.apache.wicket.settings.Settings.UnexpectedExceptionDisplay)
	 */
	public void setUnexpectedExceptionDisplay(
		final IExceptionSettings.UnexpectedExceptionDisplay unexpectedExceptionDisplay)
	{
		this.unexpectedExceptionDisplay = unexpectedExceptionDisplay;
	}
}
