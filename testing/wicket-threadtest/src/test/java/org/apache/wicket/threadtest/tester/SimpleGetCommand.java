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
package org.apache.wicket.threadtest.tester;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wicket.util.io.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO javadoc
 */
public class SimpleGetCommand extends AbstractGetCommand
{
	private static final Logger log = LoggerFactory.getLogger(SimpleGetCommand.class);

	private boolean printResponse = false;

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public SimpleGetCommand(List<String> urls, int iterations)
	{
		super(urls, iterations);
	}

	/**
	 * Construct.
	 * 
	 * @param url
	 *            URL to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public SimpleGetCommand(String url, int iterations)
	{
		super(Arrays.asList(url), iterations);
	}

	/**
	 * Gets printResponse.
	 * 
	 * @return printResponse
	 */
	public boolean getPrintResponse()
	{
		return printResponse;
	}

	/**
	 * Sets printResponse.
	 * 
	 * @param printResponse
	 *            printResponse
	 */
	public void setPrintResponse(boolean printResponse)
	{
		this.printResponse = printResponse;
	}

	/**
	 * @see org.apache.wicket.threadtest.tester.AbstractGetCommand#doGet(org.apache.commons.httpclient.HttpClient,
	 *      String)
	 */
	@Override
	protected void doGet(HttpClient client, String url) throws Exception
	{

		GetMethod method = new GetMethod(url);
		method.setFollowRedirects(true);
		try
		{
			if (url.contains("bookmarkable"))
			{
				log.info(url);
			}
			int code = client.executeMethod(method);
			if (code != 200)
			{
				log.error("ERROR! code: " + code);
				log.error(url);
				throw new Exception(new String(method.getResponseBody()));
			}
			if (getPrintResponse())
			{
				log.info("\n" + Streams.readString(method.getResponseBodyAsStream()));
			}
		}
		finally
		{
			method.releaseConnection();
		}
	}
}