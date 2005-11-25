/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.protocol.http.request;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import wicket.IRequestTarget;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebRequestCycle;
import wicket.protocol.http.WebResponse;

/**
 * Response target that is to be used in a servlet environment to send an error
 * code and optionally a message. NOTE: this target can only be used in a
 * servlet environment with {@link wicket.protocol.http.WebRequestCycle}s.
 * 
 * @author Eelco Hillenius
 */
public final class WebErrorCodeResponseTarget implements IRequestTarget
{
	/** the servlet error code. */
	private final int errorCode;

	/** the optional message to send to the client. */
	private String message;

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public WebErrorCodeResponseTarget(int errorCode)
	{
		this(errorCode, null);
	}

	/**
	 * Construct.
	 * 
	 * @param errorCode
	 *            the servlet error code; use one of the
	 *            {@link javax.servlet.http.HttpServletResponse} constants
	 * @param message
	 *            the optional message to send to the client
	 * @see javax.servlet.http.HttpServletResponse
	 */
	public WebErrorCodeResponseTarget(int errorCode, String message)
	{
		this.errorCode = errorCode;
		this.message = message;
	}

	/**
	 * Gets errorCode.
	 * 
	 * @return errorCode
	 */
	public int getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Gets message.
	 * 
	 * @return message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Respond by sending the set errorCode and optionally the message to the
	 * browser.
	 * 
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		WebResponse webResponse = ((WebRequestCycle)requestCycle).getWebResponse();
		HttpServletResponse httpServletResponse = webResponse.getHttpServletResponse();
		try
		{
			if (message != null)
			{
				httpServletResponse.sendError(errorCode, message);
			}
			else
			{
				httpServletResponse.sendError(errorCode);
			}
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return getErrorCode() + ((message != null) ? " (" + message + ")" : "");
	}
}
