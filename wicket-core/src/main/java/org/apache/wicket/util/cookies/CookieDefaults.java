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
package org.apache.wicket.util.cookies;

import org.apache.wicket.util.io.IClusterable;

/**
 * This class provides default values that are used by {@link org.apache.wicket.util.cookies.CookieUtils}
 * class when it creates cookies.
 * 
 * @author Juergen Donnerstag
 */
public class CookieDefaults implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** Max age that the component will be persisted in seconds. */
	private int maxAge = 3600 * 24 * 30; // 30 days

	/** Cookie comment. */
	private String comment;

	/** Cookie domain. */
	private String domain;

	/** Whether the cookie is secure. */
	private boolean secure;

	/** Cookie version. */
	private int version;

	/**
	 * Gets the max age. After
	 * 
	 * @return the max age
	 */
	public int getMaxAge()
	{
		return maxAge;
	}

	/**
	 * Sets the maximum age of the cookie in seconds.
	 * 
	 * @param maxAge
	 *            the max age in seconds.
	 */
	public void setMaxAge(int maxAge)
	{
		this.maxAge = maxAge;
	}

	/**
	 * Gets the cookie comment.
	 * 
	 * @return the cookie comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * Sets the cookie comment.
	 * 
	 * @param comment
	 *            the cookie comment
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * Gets the cookie domain name.
	 * 
	 * @return the cookie domain name
	 */
	public String getDomain()
	{
		return domain;
	}

	/**
	 * Sets the cookie domain name.
	 * 
	 * @param domain
	 *            the cookie domain name
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	/**
	 * Returns true if the browser is sending cookies only over a secure protocol, or false if the
	 * browser can send cookies using any protocol.
	 * 
	 * @return whether this cookie is secure
	 */
	public boolean getSecure()
	{
		return secure;
	}

	/**
	 * Indicates to the browser whether the cookie should only be sent using a secure protocol, such
	 * as HTTPS or SSL.
	 * 
	 * @param secure
	 *            if true, sends the cookie from the browser to the server using only when using a
	 *            secure protocol; if false, sent on any protocol
	 */
	public void setSecure(boolean secure)
	{
		this.secure = secure;
	}

	/**
	 * Returns the version of the protocol this cookie complies with. Version 1 complies with RFC
	 * 2109, and version 0 complies with the original cookie specification drafted by Netscape.
	 * Cookies provided by a browser use and identify the browser's cookie version.
	 * 
	 * @return 0 if the cookie complies with the original Netscape specification; 1 if the cookie
	 *         complies with RFC 2109
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * Sets the version of the cookie protocol this cookie complies with. Version 0 complies with
	 * the original Netscape cookie specification. Version 1 complies with RFC 2109. <br/>
	 * Since RFC 2109 is still somewhat new, consider version 1 as experimental; do not use it yet
	 * on production sites.
	 * 
	 * @param version
	 *            0 if the cookie should comply with the original Netscape specification; 1 if the
	 *            cookie should comply with RFC 2109
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}
}
