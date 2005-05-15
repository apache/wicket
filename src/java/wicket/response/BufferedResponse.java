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

/**
 * Holds a rendered response for future use.
 *
 * @see wicket.ApplicationSettings.RenderStrategy
 * 
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public class BufferedResponse extends StringResponse
{
	/**
	 * The url to be used to issue a client side redirect request; when a request to
	 * the url comes in, this buffered response is streamed to the browser.
	 * a request comes in.
	 */
	private String redirectUrl;

	/**
	 * The mime type of the request.
	 */
	private String mimeType;

	/**
	 * Construct.
	 * @param redirectUrl The url to be used to issue a client side redirect request;
	 * when a request to the url comes in, this buffered response is streamed to the browser.
	 */
	public BufferedResponse(String redirectUrl)
	{
		this.redirectUrl = redirectUrl;
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
		return this.out.getBuffer().length();
	}

	/**
	 * Sets the content length.
	 * @return The content type of this redirect response
	 */
	public String getContentType()
	{
		return this.mimeType;
	}
	

	/**
	 * @see wicket.Response#setContentType(java.lang.String)
	 */
	public void setContentType(String mimeType)
	{
		this.mimeType = mimeType;
	}
}