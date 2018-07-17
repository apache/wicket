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

import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;

/**
 * TODO javadoc
 */
public abstract class AbstractGetCommand extends AbstractCommand
{

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public AbstractGetCommand(List<String> urls, int iterations)
	{
		super(urls, iterations);
	}

	/**
	 * @see org.apache.wicket.threadtest.tester.Command#execute(CommandRunner)
	 */
	public void execute(CommandRunner runner) throws Exception
	{

		int iterations = getIterations();
		for (int i = 0; i < iterations; i++)
		{
			List<String> urls = getUrls();
			for (String url : urls)
			{
				final int iteration = i;
				String modUrl = new VariableInterpolator(url, false) {
					@Override
					protected String getValue(String variableName) {
						return AbstractGetCommand.this.getValue(variableName, iteration);
					}
				}.toString();
				doGet(runner.getClient(), modUrl);
			}
		}
	}

	protected String getValue(String name, int iteration) {
		if ("iteration".equals(name)) {
			return String.valueOf(iteration);
		}
		return null;
	}
	
	/**
	 * Execute a GET request using the provided url.
	 * 
	 * @param url
	 *            The url to GET
	 * @param client
	 *            the http client
	 * @throws Exception
	 */
	protected abstract void doGet(HttpClient client, String url) throws Exception;
}