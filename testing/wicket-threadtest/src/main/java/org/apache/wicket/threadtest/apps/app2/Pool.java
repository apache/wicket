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
package org.apache.wicket.threadtest.apps.app2;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test pool.
 * 
 * @author eelcohillenius
 */
public class Pool
{
	private static Pool _instance = new Pool();

	private static final Logger log = LoggerFactory.getLogger(Pool.class);

	/**
	 * @return the connection
	 */
	public static Connection getConnection()
	{
		return getInstance().doGetConnection();
	}

	/**
	 * @return the {@link Pool} instance
	 */
	public static Pool getInstance()
	{
		return _instance;
	}

	/**
	 * Releases the pool
	 */
	public static void release()
	{
		getInstance().doRelease();
	}

	private final Connection[] allConnections;

	private final Deque<Connection> available = new ArrayDeque<Connection>();

	private final ThreadLocal<Connection> locks = new ThreadLocal<Connection>();

	private final int size = 3;

	private Pool()
	{

		allConnections = new Connection[size];
		for (int i = 0; i < size; i++)
		{
			Connection connection = new Connection(String.valueOf(i));
			allConnections[i] = connection;
			available.push(connection);
		}
	}

	private synchronized Connection doGetConnection()
	{

		Connection c = locks.get();

		if (c != null)
		{
			return c;

		}
		else
		{

			while (c == null)
			{

				if (!available.isEmpty())
				{
					c = available.pop();
					locks.set(c);
					log.info("returning " + c + " for " + Thread.currentThread());
				}
				else
				{
					try
					{
						log.info("enter wait for " + Thread.currentThread());
						wait();
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		return c;
	}

	private synchronized void doRelease()
	{
		Connection c = locks.get();
		if (c != null)
		{
			available.push(c);
			locks.remove();
			log.info("releasing " + c + " for " + Thread.currentThread());
			notifyAll();
		}
	}
}
