/*
 * $Id$
 * $Revision$
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
package wicket.response;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Response;
import wicket.WicketRuntimeException;

/**
 * Holds a rendered response for future use.
 * 
 * @see wicket.ApplicationSettings.RenderStrategy
 * 
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class BufferedResponse extends Response
{
	/** Log. */
	private static final Log log = LogFactory.getLog(BufferedResponse.class);

	/**
	 * The url to be used to issue a client side redirect request; when a
	 * request to the url comes in, this buffered response is streamed to the
	 * browser. a request comes in.
	 */
	private String redirectUrl;

	/**
	 * A buffer for building up the string when the response is filled/created.
	 */
	private StringBuffer stringBuffer;

	/**
	 * The byte buffer that holds the encoded string when this response is
	 * closed. The content StringBuffer is converted using the right encoding.
	 */
	private byte[] byteBuffer;

	/**
	 * The mime type of the request.
	 */
	private String mimeType;

	/**
	 * Construct.
	 * 
	 */
	public BufferedResponse()
	{
		this.stringBuffer = new StringBuffer();
	}

	/**
	 * The url to be used to issue a client side redirect request; when a
	 * request to the url comes in, this buffered response is streamed to the
	 * browser.
	 * 
	 * @return The redirect url that is used for this response
	 */
	public final String getRedirectUrl()
	{
		return redirectUrl;
	}

	/**
	 * Gets the content length.
	 * <p>
	 * Note: Prior to closing the response, the number of characters is
	 * returned. After closing, the number of bytes is returned, which depending
	 * on the encoding and the characters might be larger.
	 * 
	 * @return The content length of this redirect response
	 */
	public final int getContentLength()
	{
		if (byteBuffer != null)
		{
			return byteBuffer.length;
		}

		return stringBuffer.length();
	}

	/**
	 * Get the content mime type.
	 * 
	 * @return The content type of this redirect response
	 */
	public final String getContentType()
	{
		return this.mimeType;
	}

	/**
	 * Sets the content mimetype.
	 * 
	 * @param mimeType
	 */
	public final void setContentType(final String mimeType)
	{
		this.mimeType = mimeType;
	}

	/**
	 * @see wicket.Response#redirect(java.lang.String)
	 */
	public final void redirect(final String url)
	{
		redirectUrl = url;
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	public final OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException("Cannot get output stream on BufferedResponse");
	}

	/**
	 * @see wicket.Response#write(java.lang.String)
	 */
	public final void write(final String string)
	{
		if (stringBuffer == null)
		{
			throw new WicketRuntimeException("Can not write to the response after closing it.");
		}
		stringBuffer.append(string);
	}

	/**
	 * @see wicket.Response#close()
	 */
	public final void close()
	{
		if (stringBuffer == null)
		{
			throw new WicketRuntimeException("The response has already been closed.");
		}

		super.close();

		this.byteBuffer = convertToCharset(getCharacterEncoding());
		this.stringBuffer = null;
	}

	/**
	 * Get the bytes of this buffered response string in the encoding of the
	 * mime type.
	 * 
	 * @return after closing the response, the encoded bytes, else null.
	 */
	public final byte[] getBytes()
	{
		return byteBuffer;
	}

	/**
	 * Prior to closing the response, the current content of the buffer. After
	 * closing the response, an exception will be thrown as the buffer is no
	 * longer available.
	 * 
	 * @return String The buffer content
	 */
	public final String getString()
	{
		if (stringBuffer == null)
		{
			throw new WicketRuntimeException("You can call getString() after closing the response.");
		}
		return stringBuffer.toString();
	}

	/**
	 * Convert the string into the output encoding required
	 * 
	 * @param encoding
	 *            The output encoding
	 * @return byte[] The encoded characters converted into bytes
	 */
	private byte[] convertToCharset(final String encoding)
	{
		if (encoding == null)
		{
			throw new WicketRuntimeException("Internal error: encoding must not be null");
		}

		final String string = getString();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(string.length());

		final OutputStreamWriter osw;
		final byte[] bytes;
		try
		{
			osw = new OutputStreamWriter(baos, encoding);
			osw.write(string);
			osw.close();

			bytes = baos.toByteArray();
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException("Can't convert response to charset: " + encoding, ex);
		}

		return bytes;
	}
}