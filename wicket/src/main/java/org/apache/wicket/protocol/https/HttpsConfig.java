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
 * @see HttpsRequestCycleProcessor
 */
public class HttpsConfig
{
	private int httpPort;
	private int httpsPort;

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
}
