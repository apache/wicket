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
package org.apache.wicket.protocol.ws.api.message;

/**
 * A {@link IWebSocketMessage message} with binary data
 *
 * @since 6.0
 */
public class BinaryMessage implements IWebSocketMessage
{
	private final byte[] data;
	private final int offset;
	private final int length;

	/**
	 * Constructor.
	 *
	 * @param data
	 *      the binary message from the client
	 * @param offset
	 *      the offset to read from
	 * @param length
	 *      how much data to read
	 */
	public BinaryMessage(byte[] data, int offset, int length)
	{
		this.data = data;
		this.offset = offset;
		this.length = length;
	}

	public final byte[] getData()
	{
		return data;
	}

	public final int getOffset()
	{
		return offset;
	}

	public final int getLength()
	{
		return length;
	}
}