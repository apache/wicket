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
	 * Gets the servlet error code.
	 * 
	 * @return the servlet error code
	 */
	public final int getErrorCode()
	{
		return errorCode;
	}

	/**
	 * Gets the optional message to send to the client.
	 * 
	 * @return the optional message to send to the client
	 */
	public final String getMessage()
	{
		return message;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(RequestCycle requestCycle)
	{
		return null;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj instanceof WebErrorCodeResponseTarget)
		{
			WebErrorCodeResponseTarget that = (WebErrorCodeResponseTarget)obj;
			if (errorCode == that.errorCode)
			{
				if (message != null)
				{
					equal = (that.message != null && message.equals(that.message));
				}
				else
				{
					equal = (that.message == null);
				}
			}
		}
		return equal;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "WebErrorCodeResponseTarget".hashCode();
		result += message != null ? message.hashCode() : 0;
		result += errorCode;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[WebErrorCodeResponseTarget@" + hashCode() + " errorCode=" + getErrorCode()
				+ ((message != null) ? " (" + message + ")" : "" + "]");
	}
}
