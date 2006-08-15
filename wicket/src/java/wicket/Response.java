/*
 * $Id$ $Revision:
 * 1.6 $ $Date$
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
package wicket;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import wicket.markup.ComponentTag;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.util.time.Time;

/**
 * Abstract base class for different implementations of response writing. A
 * subclass must implement write(String) to write a String to the response
 * destination (whether it be a browser, a file, a test harness or some other
 * place). A subclass may optionally implement close(), encodeURL(String),
 * redirect(String), isRedirect() or setContentType(String) as appropriate.
 * 
 * @author Jonathan Locke
 */
public abstract class Response
{
    /** Default encoding of output stream */
    private String defaultEncoding;

	/**
	 * Closes the response output stream
	 */
	public void close()
	{
	}
	
	/**
	 * Called when the Response needs to reset itself.
	 * Subclasses can empty there buffer or build up state.
	 */
	public void reset()
	{
		
	}

	/**
	 * An implementation of this method is only required if a subclass wishes to
	 * support sessions via URL rewriting. This default implementation simply
	 * returns the URL String it is passed.
	 * 
	 * @param url
	 *            The URL to encode
	 * @return The encoded url
	 */
	public CharSequence encodeURL(final CharSequence url)
	{
		return url;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * Loops over all the response filters that were set (if any) with the give
	 * response returns the response buffer itself if there where now filters or
	 * the response buffer that was created/returned by the filter(s)
	 * 
	 * @param responseBuffer
	 *            The response buffer to be filtered
	 * @return Returns the filtered string buffer.
	 */
	public final AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
	{
		List responseFilters = Application.get().getRequestCycleSettings().getResponseFilters();
		if (responseFilters == null)
		{
			return responseBuffer;
		}
		for (int i = 0; i < responseFilters.size(); i++)
		{
			IResponseFilter filter = (IResponseFilter)responseFilters.get(i);
			responseBuffer = filter.filter(responseBuffer);
		}
		return responseBuffer;
	}

	/**
	 * Get the default encoding
	 * 
	 * @return default encoding
	 */
	public String getCharacterEncoding()
	{
		if (this.defaultEncoding == null)
		{
			return Application.get().getRequestCycleSettings().getResponseRequestEncoding();
		}
		else
		{
			return this.defaultEncoding;
		}
	}

	/**
	 * @return The output stream for this response
	 */
	public abstract OutputStream getOutputStream();

	/**
	 * Returns true if a redirection has occurred. The default implementation
	 * always returns false since redirect is not implemented by default.
	 * 
	 * @return True if the redirect method has been called, making this response
	 *         a redirect.
	 */
	public boolean isRedirect()
	{
		return false;
	}

	/**
	 * A subclass may override this method to implement redirection. Subclasses
	 * which have no need to do redirection may choose not to override this
	 * default implementation, which does nothing. For example, if a subclass
	 * wishes to write output to a file or is part of a testing harness, there
	 * may be no meaning to redirection.
	 * 
	 * @param url
	 *            The URL to redirect to
	 */
	public void redirect(final String url)
	{
	}

	/**
	 * Set the default encoding for the output. 
	 * Note: It is up to the derived class to make use of the information.
	 * Class Respsonse simply stores the value, but does not apply
	 * it anywhere automatically.
	 * 
	 * @param encoding
	 */
	public void setCharacterEncoding(final String encoding)
	{
	    this.defaultEncoding = encoding;
	}
	
	/**
	 * Set the content length on the response, if appropriate in the subclass.
	 * This default implementation does nothing.
	 * 
	 * @param length
	 *            The length of the content
	 */
	public void setContentLength(final long length)
	{
	}

	/**
	 * Set the content type on the response, if appropriate in the subclass.
	 * This default implementation does nothing.
	 * 
	 * @param mimeType
	 *            The mime type
	 */
	public void setContentType(final String mimeType)
	{
	}

	/**
	 * Set the contents last modified time, if appropriate in the subclass.
	 * This default implementation does nothing.
	 * @param time 
	 *				The time object 
	 */
	public void setLastModifiedTime(Time time)
	{
	}
	
	/**
	 * @param locale
	 *            Locale to use for this response
	 */
	public void setLocale(final Locale locale)
	{
	}
	
	/**
	 * Writes the given tag to via the write(String) abstract method.
	 * 
	 * @param tag
	 *            The tag to write
	 */
	public final void write(final ComponentTag tag)
	{
		write(tag.toString());
	}

	/**
	 * Writes the given string to the Response subclass output destination.
	 * 
	 * @param string
	 *            The string to write
	 */
	public abstract void write(final CharSequence string);

	/**
	 * Writes the given string to the Response subclass output destination and
	 * appends a cr/nl depending on the OS
	 * 
	 * @param string
	 */
	public final void println(final CharSequence string)
	{
		write(string);
		write(Strings.LINE_SEPARATOR);
	}
}
