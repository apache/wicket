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
package org.apache.wicket.islands;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Thrown to reject an island call. The client's call fails with the kind <code>invalid</code> and
 * the given messages; whatever the handler added to the Ajax request target before rejecting is
 * still sent.
 *
 * @see IslandCall#reject(String...)
 * @since 12.0.0
 */
public class IslandCallRejectedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private final List<String> messages;

	/**
	 * @param messages
	 *            the messages for the client; a {@code null} message, such as the message of a
	 *            caught exception that has none, is dropped
	 */
	public IslandCallRejectedException(String... messages)
	{
		this(Arrays.stream(messages).filter(Objects::nonNull).toList());
	}

	private IslandCallRejectedException(List<String> messages)
	{
		super(String.join("; ", messages), null, false, false);
		this.messages = messages;
	}

	/**
	 * @return the messages for the client
	 */
	public List<String> getMessages()
	{
		return messages;
	}
}
