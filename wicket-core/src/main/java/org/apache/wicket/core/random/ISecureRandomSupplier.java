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
package org.apache.wicket.core.random;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Supplies the Wicket application with random bytes.
 * 
 * @author papegaaij
 */
public interface ISecureRandomSupplier
{
	/**
	 * Returns the actual {@code SecureRandom} being used as source.
	 * 
	 * @return The {@code SecureRandom}.
	 */
	public SecureRandom getRandom();

	/**
	 * Returns a byte array with random bytes of the given length.
	 * 
	 * @param length
	 *            The number of bytes to return.
	 * @return A byte array with random bytes of the given length.
	 */
	public default byte[] getRandomBytes(int length)
	{
		byte[] ret = new byte[length];
		getRandom().nextBytes(ret);
		return ret;
	}
	
	/**
	 * Returns a base64 encoded string with random content, base on {@code length} bytes. The length
	 * of the returned string will be {@code length/3*4}.
	 * 
	 * @param length
	 *            The number of random bytes to use as input.
	 * @return A string with random base64 data.
	 */
	public default String getRandomBase64(int length)
	{
		return Base64.getUrlEncoder().encodeToString(getRandomBytes(length));
	}
}
