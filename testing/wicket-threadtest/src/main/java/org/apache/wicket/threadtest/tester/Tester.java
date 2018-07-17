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
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.wicket.threadtest.tester.CommandRunner.CommandRunnerObserver;
import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author eelcohillenius
 */
public final class Tester implements CommandRunnerObserver
{
	private static final Logger log = LoggerFactory.getLogger(Tester.class);

	private static HttpClientParams params;

	static
	{
		params = new HttpClientParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
	}

	/**
	 * Main method for just starting the server
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// start server on its own
		int port = 8090;
		if (args.length > 0)
		{
			port = Integer.valueOf(args[0]);
		}
		Server server = startServer(port);
		try
		{
			server.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				server.stop();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			System.exit(1);
		}
	}

	/**
	 * Start Jetty server instance and return the handle.
	 * 
	 * @param port
	 * @return server handle
	 */
	private static Server startServer(int port)
	{
		Server server;
		// start up server
		server = new Server(port);
		WebAppContext ctx = new WebAppContext("./src/main/webapp", "/");
		server.setHandler(ctx);
		try
		{
			server.start();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return server;
	}

	private int activeThreads = 0;

	private final List<Command> commands;

	private String host = "localhost";

	/**
	 * if true, each thread will represent a seperate session. If false, the test behaves like one
	 * client issuing multiple concurrent requests.
	 */
	private final boolean multipleSessions;

	private final int numberOfThreads;

	private int port = 8090;

	/**
	 * Construct.
	 * 
	 * @param command
	 *            Command to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all commands
	 * @param multipleSessions
	 *            if true, each thread will represent a seperate session. If false, the test behaves
	 *            like one client issuing multiple concurrent requests
	 */
	public Tester(Command command, int numberOfThreads, boolean multipleSessions)
	{
		this(Arrays.asList(command), numberOfThreads, multipleSessions);
	}

	/**
	 * Construct.
	 * 
	 * @param commands
	 *            Commands to execute
	 * @param numberOfThreads
	 *            Number of threads to run the commands. Each thread runs all commands
	 * @param multipleSessions
	 *            if true, each thread will represent a separate session. If false, the test behaves
	 *            like one client issuing multiple concurrent requests
	 */
	public Tester(List<Command> commands, int numberOfThreads, boolean multipleSessions)
	{
		this.commands = commands;
		this.numberOfThreads = numberOfThreads;
		this.multipleSessions = multipleSessions;
	}

	/**
	 * Gets host.
	 * 
	 * @return host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * Gets port.
	 * 
	 * @return port
	 */
	public int getPort()
	{
		return port;
	}

	public synchronized void onDone(CommandRunner runner)
	{
		activeThreads--;
		notifyAll();
	}

	public synchronized void onError(CommandRunner runner, Exception e)
	{
		activeThreads--;
		notifyAll();
	}

	/**
	 * Runs the test.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception
	{

		activeThreads = 0;

		HttpConnectionManagerParams connManagerParams = new HttpConnectionManagerParams();
		connManagerParams.setDefaultMaxConnectionsPerHost(numberOfThreads * 2);
		MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
		manager.setParams(connManagerParams);

		Server server = null;
		GetMethod getMethod = new GetMethod("http://localhost:" + port + "/");
		try
		{
			getMethod.setFollowRedirects(true);
			HttpClient httpClient = new HttpClient(params, manager);
			int code = httpClient.executeMethod(getMethod);
			if (code != 200)
			{
				server = startServer(port);
			}
		}
		catch (Exception e)
		{
			server = startServer(port);
		}
		finally
		{
			getMethod.releaseConnection();
		}

		try
		{

			ThreadGroup g = new ThreadGroup("runners");
			Thread[] threads = new Thread[numberOfThreads];
			HttpClient client = null;
			for (int i = 0; i < numberOfThreads; i++)
			{

				if (multipleSessions)
				{
					client = new HttpClient(params, manager);
					client.getHostConfiguration().setHost(host, port);
				}
				else
				{
					if (client == null)
					{
						client = new HttpClient(params, manager);
						client.getHostConfiguration().setHost(host, port);
					}
				}
				threads[i] = new Thread(g, new CommandRunner(commands, client, this));
			}

			long start = System.currentTimeMillis();

			for (int i = 0; i < numberOfThreads; i++)
			{
				activeThreads++;
				threads[i].start();
			}

			while (activeThreads > 0)
			{
				synchronized (this)
				{
					wait();
				}
			}

			long end = System.currentTimeMillis();
			long time = end - start;
			log.info("\n******** finished in " + Duration.milliseconds(time) + " (" + time +
				" milis)");

		}
		finally
		{
			MultiThreadedHttpConnectionManager.shutdownAll();
			if (server != null)
			{
				server.stop();
			}
		}
	}

	/**
	 * Sets host.
	 * 
	 * @param host
	 *            host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Sets port.
	 * 
	 * @param port
	 *            port
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
}
