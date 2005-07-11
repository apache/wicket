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
	/**
	 * The default char encoding that is used for String->bytes
	 */
	public static final String DEFAULT_CHARACTER_ENCODING="ISO-8859-1";
	
	/** Log. */
	private static final Log log = LogFactory.getLog(BufferedResponse.class);
	
	/**
	 * The url to be used to issue a client side redirect request; when a request to
	 * the url comes in, this buffered response is streamed to the browser.
	 * a request comes in.
	 */
	private String redirectUrl;
	

	/**
	 * A buffer for building up the string when the response is filled/created.
	 */
	private StringBuffer stringBuffer;
	
	/**
	 * The byte buffer that holds the encoded string when this response is closed 
	 * The content StringBuffer is converted using the right encoding. 
	 */
	private byte[] byteBuffer;
	
	/**
	 * The mime type of the request.
	 */
	private String mimeType;


	private String charset;

	/**
	 * Construct.
	 * @param redirectUrl The url to be used to issue a client side redirect request;
	 * when a request to the url comes in, this buffered response is streamed to the browser.
	 */
	public BufferedResponse(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
		this.stringBuffer = new StringBuffer();
	}
	
	/**
	 * The url to be used to issue a client side redirect request; when a request to
	 * the url comes in, this buffered response is streamed to the browser.
	 * @return The redirect url that is used for this response
	 */
	public String getRedirectUrl()
	{
		return redirectUrl;
	}
	
	/**
	 * Gets the content length.
	 * @return The content length of this redirect response
	 */
	public int getContentLength()
	{
		if(byteBuffer != null) return byteBuffer.length;
		return stringBuffer.length();
	}

	/**
	 * @return The content type of this redirect response
	 */
	public String getContentType()
	{
		return this.mimeType;
	}
	
	
	/**
	 * Set the charset to be used for string encoding.
	 * @param charset The charsest name
	 */
	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	/**
	 * Get the charset that is or will be used for string encoding.
	 * @return The charset used for encoding the string to  bytes
	 */
	public String getCharset()
	{
		return this.charset;
	}

	/**
	 * Sets the content mimetype.
	 * @param mimeType 
	 */
	public void setContentType(String mimeType)
	{
		this.mimeType = mimeType;
	}
	
	/**
	 * @see wicket.Response#redirect(java.lang.String)
	 */
	public void redirect(String url)
	{
		redirectUrl = url;
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException("Cannot get output stream on BufferedResponse");
	}

	/**
	 * @see wicket.Response#write(java.lang.String)
	 */
	public void write(String string)
	{
		stringBuffer.append(string);
	}
	
	/**
	 * @see wicket.Response#close()
	 */
	public void close()
	{
		super.close();
		
		String mimeCharset = null;
		if(mimeType != null)
		{
			int index = mimeType.indexOf("charset");
			if(index != -1)
			{
				index = mimeType.indexOf('=', index+7);
				if(index != -1)
				{
					// TODO better parsing of this charset.. can string be after this..
					mimeCharset = mimeType.substring(index+1);
				}
			}
		}
		byteBuffer = convertToCharset(mimeCharset,charset);
		stringBuffer = null;
	}
	
	/**
	 * Get the bytes of this buffered response string in the encoding of the mime type.
	 * @return the encoded bytes
	 */
	public byte[] getBytes()
	{
		return byteBuffer;
	}
	
	public String getString()
	{
		return stringBuffer.toString();
	}
	
	private byte[] convertToCharset(String charset, String backupCharset)
	{
		if(charset == null)
		{
			charset = backupCharset;
			backupCharset = null;
		}
		byte[] bytes = null;
		String string = stringBuffer.toString();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(string.length());
		if(charset != null)
		{
			OutputStreamWriter osw = null;
			try
			{
				osw = new OutputStreamWriter(baos,charset);
				osw.write(string);
				osw.close();
				bytes = baos.toByteArray();
				this.charset = charset;
			}
			catch (Exception ex)
			{
				log.debug("Can't convert response to charset: " + charset, ex);
				if(backupCharset != null)
				{
					try
					{
						osw = new OutputStreamWriter(baos,backupCharset);
						osw.write(string);
						osw.close();
						bytes = baos.toByteArray();
						this.charset = backupCharset;
					}
					catch (Exception ex1)
					{
						log.debug("Can't convert response to charset: " + backupCharset, ex1);
					}
				}
			}
		}
		if(bytes == null)
		{
			OutputStreamWriter osw = null;
			try
			{
				osw = new OutputStreamWriter(baos,DEFAULT_CHARACTER_ENCODING);
				osw.write(string);
				osw.close();
				bytes = baos.toByteArray();
				this.charset = DEFAULT_CHARACTER_ENCODING;
			}
			catch (Exception ex1)
			{
				bytes = string.getBytes();
			}
		}
		return bytes;
	}
}