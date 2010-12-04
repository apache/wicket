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
package org.apache.wicket.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream that counts bytes written to it (but discards them).
 * 
 * @author Jonathan Locke
 */
public final class ByteCountingOutputStream extends OutputStream
{
	private long size;

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(final int b) throws IOException
	{
		size++;
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte b[], final int off, final int len) throws IOException
	{
		size += len;
	}

	/**
	 * @return Number of bytes written to this stream
	 */
	public long size()
	{
		return size;
	}
}
