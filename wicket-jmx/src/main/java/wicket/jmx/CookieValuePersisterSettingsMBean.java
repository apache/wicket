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
package wicket.jmx;


/**
 * MBean interface for exposing application related information and
 * functionality.
 * 
 * @author eelcohillenius
 */
public interface CookieValuePersisterSettingsMBean
{
	/**
	 * Gets the cookie comment.
	 * 
	 * @return the cookie comment
	 */
	String getComment();

	/**
	 * Gets the cookie domain name.
	 * 
	 * @return the cookie domain name
	 */
	String getDomain();

	/**
	 * Gets the max age. After
	 * 
	 * @return the max age
	 */
	int getMaxAge();

	/**
	 * Returns true if the browser is sending cookies only over a secure
	 * protocol, or false if the browser can send cookies using any protocol.
	 * 
	 * @return whether this cookie is secure
	 */
	boolean getSecure();

	/**
	 * Returns the version of the protocol this cookie complies with. Version 1
	 * complies with RFC 2109, and version 0 complies with the original cookie
	 * specification drafted by Netscape. Cookies provided by a browser use and
	 * identify the browser's cookie version.
	 * 
	 * @return 0 if the cookie complies with the original Netscape
	 *         specification; 1 if the cookie complies with RFC 2109
	 */
	int getVersion();

	/**
	 * Sets the cookie comment.
	 * 
	 * @param comment
	 *            the cookie comment
	 */
	void setComment(String comment);

	/**
	 * Sets the cookie domain name.
	 * 
	 * @param domain
	 *            the cookie domain name
	 */
	void setDomain(String domain);

	/**
	 * Sets the maximum age of the cookie in seconds.
	 * 
	 * @param maxAge
	 *            the max age in secs.
	 */
	void setMaxAge(int maxAge);

	/**
	 * Indicates to the browser whether the cookie should only be sent using a
	 * secure protocol, such as HTTPS or SSL.
	 * 
	 * @param secure
	 *            if true, sends the cookie from the browser to the server using
	 *            only when using a secure protocol; if false, sent on any
	 *            protocol
	 */
	void setSecure(boolean secure);

	/**
	 * Sets the version of the cookie protocol this cookie complies with.
	 * Version 0 complies with the original Netscape cookie specification.
	 * Version 1 complies with RFC 2109. <br/>Since RFC 2109 is still somewhat
	 * new, consider version 1 as experimental; do not use it yet on production
	 * sites.
	 * 
	 * @param version
	 *            0 if the cookie should comply with the original Netscape
	 *            specification; 1 if the cookie should comply with RFC 2109
	 */
	void setVersion(int version);
}
