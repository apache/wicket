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
package org.apache._wicket.request.response;

/**
 * Abstract base class for different implementations of response writing.
 * <p>
 * The implementation may not support calling both {@link #write(byte[])} and
 * {@link #write(CharSequence)} on the same {@link Response} instance.
 * 
 * @author Matej Knopp
 */
public abstract class Response
{
	/**
	 * Writes the {@link CharSequence} to output.
	 * 
	 * @param sequence
	 * @throws IllegalStateException
	 *             if {@link #write(byte[])} has already been called on this instance
	 */
	public abstract void write(CharSequence sequence);

	/**
	 * Writes the buffer to output.
	 * 
	 * @param array
	 * @throws IllegalStateException
	 *             if {@link #write(CharSequence)} has already been called on this instance
	 */
	public abstract void write(byte[] array);

	/**
	 * Closes the response
	 */
	public void close()
	{
	}
}
