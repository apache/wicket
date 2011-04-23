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

/**
 * TODO javadoc
 */
public abstract class AbstractCommand implements Command
{

	/** number of executions of the urls. */
	private final int iterations;

	/** URLs to visit. */
	private final List<String> urls;

	/**
	 * Construct.
	 * 
	 * @param urls
	 *            URLs to visit
	 * @param iterations
	 *            number of executions of the urls
	 */
	public AbstractCommand(List<String> urls, int iterations)
	{
		this.urls = urls;
		this.iterations = iterations;
	}

	/**
	 * Gets iterations.
	 * 
	 * @return iterations
	 */
	public int getIterations()
	{
		return iterations;
	}

	/**
	 * Gets urls.
	 * 
	 * @return urls
	 */
	public List<String> getUrls()
	{
		return urls;
	}
}