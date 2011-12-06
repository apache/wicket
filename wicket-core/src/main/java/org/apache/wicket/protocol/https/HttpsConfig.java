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
package org.apache.wicket.protocol.https;

/**
 * Configuration for http-https switching
 * 
 * @see HttpsMapper
 */
public class HttpsConfig
{
	private int httpPort;
	private int httpsPort;

	/**
	 * A flag which can be used to configure {@link HttpsMapper} to bind or not the session before
	 * switching to secure (https) mode
	 */
	private boolean preferStateful = true;

	/**
	 * Constructor
	 */
	public HttpsConfig()
	{
		this(80, 443);
	}

	/**
	 * Constructor
	 * 
	 * @param httpPort
	 *            http port
	 * @param httpsPort
	 *            https port
	 */
	public HttpsConfig(int httpPort, int httpsPort)
	{
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
	}


	/**
	 * Sets http port
	 * 
	 * @param httpPort
	 */
	public void setHttpPort(int httpPort)
	{
		this.httpPort = httpPort;
	}

	/**
	 * Sets https port
	 * 
	 * @param httpsPort
	 */
	public void setHttpsPort(int httpsPort)
	{
		this.httpsPort = httpsPort;
	}

	/**
	 * @return http port
	 */
	public int getHttpPort()
	{
		return httpPort;
	}

	/**
	 * @return https port
	 */
	public int getHttpsPort()
	{
		return httpsPort;
	}

	/**
	 * @see #setPreferStateful(boolean)
	 * @return preferStateless
	 */
	public boolean isPreferStateful()
	{
		return preferStateful;
	}

	/**
	 * Sets whether or not a new session is created before redirecting from {@code http} to
	 * {@code https}
	 * <p>
	 * BE VERY CAREFUL WHEN SETTING THIS VALUE TO {@code false}.
	 * 
	 * If set to {@code false} it is possible that the session created when in {@code https} pages
	 * will not be accessible to {@code http} pages, and so you may end up with two sessions per
	 * user both potentially containing different login information.
	 * </p>
	 * 
	 * @param preferStateful
	 */
	public void setPreferStateful(boolean preferStateful)
	{
		this.preferStateful = preferStateful;
	}
}
