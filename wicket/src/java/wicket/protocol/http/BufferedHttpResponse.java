/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;

/**
 * Subclass of HttpResponse which buffers output and any redirection.
 * 
 * @author Jonathan Locke
 */
public class BufferedHttpResponse extends HttpResponse
{
	/** Log. */
	private static final Log log = LogFactory.getLog(BufferedHttpResponse.class);

	/** URL to redirect to when response is flushed, if any */
	private String redirectUrl;

	/** Buffer to hold page */
	private StringBuffer buffer = new StringBuffer(1024);

	/**
	 * Constructor for testing harness.
	 */
	BufferedHttpResponse()
	{
	}

	/**
	 * Package private constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 * @throws IOException
	 */
	BufferedHttpResponse(final HttpServletResponse httpServletResponse) throws IOException
	{
		super(httpServletResponse);
	}

	/**
     * Flushes the response buffer by doing a redirect or writing out the buffer.
     * NOTE: The servlet container will close the response output stream.
	 */
	public void close()
	{
        // If a redirection was specified
        if (redirectUrl != null)
        {
            // actually redirect
            super.redirect(redirectUrl);
        }
        else
        {
            // Write the buffer to the response stream
            if (buffer.length() != 0)
            {
                super.write(buffer.toString());
            }
        }
	}

	/**
	 * Saves url to redirect to when buffered response is flushed.
	 * 
	 * @param url
	 *            The URL to redirect to
	 */
	public final void redirect(final String url)
	{
        if (redirect)
        {
        	throw new WicketRuntimeException("Already redirecting to '" + redirectUrl + "'. Cannot redirect more than once");
        }
		super.redirect = true;
		this.redirectUrl = url;
	}

	/**
	 * Writes string to response output.
	 * 
	 * @param string
	 *            The string to write
	 */
	public void write(final String string)
	{
		buffer.append(string);
	}
}