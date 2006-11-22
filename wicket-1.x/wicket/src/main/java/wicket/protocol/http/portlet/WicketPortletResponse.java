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
package wicket.protocol.http.portlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebResponse;

/**
 * A Response implementation that uses PortletResponse
 * 
 * @see Response
 * @see WebResponse
 * 
 * @author Janne Hietam&auml;ki
 */
public class WicketPortletResponse extends Response
{

	/** The underlying response object. */
	PortletResponse res;

	/**
	 * @param res
	 */
	public WicketPortletResponse(PortletResponse res)
	{
		this.res = res;
	}

	/*
	 * @see wicket.Response#getOutputStream()
	 */
	public OutputStream getOutputStream()
	{
		try
		{
			if (res instanceof RenderResponse)
			{
				return ((RenderResponse)res).getPortletOutputStream();
			}
			else
			{
				throw new WicketRuntimeException("OutputStream not available during ActionRequest");
			}
		}
		catch (final IOException e)
		{
			throw new WicketRuntimeException("Error while writing to portlet output writer.", e);
		}
	}

	/*
	 * @see wicket.Response#write(java.lang.CharSequence)
	 */
	public void write(final CharSequence string)
	{
		try
		{
			if (res instanceof RenderResponse)
			{
				((RenderResponse)res).getWriter().write(string.toString());
			}
			else
			{
				throw new WicketRuntimeException("Writer not available during ActionRequest");
			}
		}
		catch (final IOException e)
		{
			throw new WicketRuntimeException("Error while writing to portlet output writer.", e);
		}
	}

	/*
	 * @see wicket.Response#setContentType(java.lang.String)
	 */
	public void setContentType(final String mimeType)
	{
		if (res instanceof RenderResponse)
		{
			RenderResponse r = (RenderResponse)res;
			r.setContentType(mimeType);
		}
	}

	/*
	 * @see wicket.Response#encodeURL(java.lang.CharSequence)
	 */
	public CharSequence encodeURL(CharSequence url)
	{
		return res.encodeURL(url.toString());
	}

	/*
	 * @see wicket.Response#redirect(java.lang.String)
	 */
	public void redirect(String url)
	{
		throw new WicketRuntimeException("Portlet can't send a redirect");
	}


	/**
	 * Gets the wrapped portlet response object.
	 * 
	 * @return The wrapped portlet response object
	 */
	public final PortletResponse getPortletResponse()
	{
		return res;
	}
}
