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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs a command.
 * 
 * @author eelcohillenius
 */
public class CommandRunner implements Runnable
{

	public static interface CommandRunnerObserver
	{

		void onDone(CommandRunner runner);

		void onError(CommandRunner runner, Exception e);
	}

	private static final Log log = LogFactory.getLog(CommandRunner.class);

	private HttpClient client;

	private final List<Command> commands;

	private final CommandRunnerObserver observer;

	/**
	 * Construct.
	 * 
	 * @param commands
	 * @param client
	 */
	public CommandRunner(List<Command> commands, HttpClient client, CommandRunnerObserver observer)
	{
		this.commands = commands;
		this.client = client;
		this.observer = observer;
	}

	/**
	 * Gets the HTTP client.
	 * 
	 * @return the HTTP client
	 */
	public HttpClient getClient()
	{
		return this.client;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{

		for (Command command : commands)
		{
			try
			{
				command.execute(this);
			}
			catch (Exception e)
			{
				log.fatal("execution of command " + command + ", thread " + Thread.currentThread() +
					" failed", e);
				observer.onError(this, e);
				return;
			}
		}
		observer.onDone(this);
	}
}