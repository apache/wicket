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
package org.apache.wicket.response;

import org.apache.wicket.request.Response;


/**
 * Response implementation that discards all output.
 * 
 * @author Jonathan Locke
 */
public class NullResponse extends Response
{
	/** The one and only instance of NullResponse */
	private static final NullResponse instance = new NullResponse();

	/**
	 * Private constructor to force use of static factory method.
	 */
	private NullResponse()
	{
	}

	/**
	 * @return The one and only instance of NullResponse
	 */
	public static NullResponse getInstance()
	{
		return instance;
	}

	/**
	 * @see org.apache.wicket.request.Response#write(CharSequence)
	 */
	@Override
	public void write(CharSequence string)
	{
		// Does nothing
	}

	@Override
	public void write(byte[] array)
	{
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return url != null ? url.toString() : null;
	}

	@Override
	public Object getContainerResponse()
	{
		return null;
	}
}
