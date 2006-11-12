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
package wicket.response;

import java.io.OutputStream;

import wicket.Response;

/**
 * A Response implementation which writes to the console.
 * 
 * @author Jonathan Locke
 */
public class ConsoleResponse extends Response
{
	/** The one and only instance */
	private static final ConsoleResponse instance = new ConsoleResponse();

	/**
	 * @return The one and only instance of NullResponse
	 */
	public static final ConsoleResponse getInstance()
	{
		return instance;
	}

	/**
	 * Private constructor to force use of static factory method
	 */
	private ConsoleResponse()
	{
	}

	/**
	 * @see wicket.Response#write(CharSequence)
	 */
	@Override
	public void write(CharSequence string)
	{
		System.out.print(string);
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream()
	{
		return System.out;
	}
}
