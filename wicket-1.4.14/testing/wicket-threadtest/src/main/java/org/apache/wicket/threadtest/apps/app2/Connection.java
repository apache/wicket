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

import org.apache.wicket.Session;

/**
 */
public class Connection
{

	private final String id;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the connection id
	 */
	public Connection(String id)
	{
		this.id = id;
	}

	@Override
	public boolean equals(Object obj)
	{
		return ((Connection)obj).id.equals(id);
	}

	/**
	 * @return information about the connection, session and the current thread
	 */
	public String getData()
	{
		return "data[Connection=" + id + ",Session=" + Session.get() + ",Thread=" +
			Thread.currentThread() + "]";
	}

	/**
	 * Gets id.
	 * 
	 * @return id
	 */
	public String getId()
	{
		return id;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public String toString()
	{
		return "Connection[" + id + "]";
	}
}