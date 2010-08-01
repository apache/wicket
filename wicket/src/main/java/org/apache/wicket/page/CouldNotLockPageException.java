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
package org.apache.wicket.page;

import org.apache.wicket.util.time.Duration;

public class CouldNotLockPageException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final int page;
	private final Duration timeout;
	private final String threadName;

	public CouldNotLockPageException(int page, String threadName, Duration timeout)
	{
		super("Could not lock page " + page + ". Attempt lasted " + timeout);
		this.page = page;
		this.timeout = timeout;
		this.threadName = threadName;
	}

	public int getPage()
	{
		return page;
	}

	public Duration getTimeout()
	{
		return timeout;
	}

	public String getThreadName()
	{
		return threadName;
	}


}
