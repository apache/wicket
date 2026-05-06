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

import org.apache.wicket.WicketRuntimeException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A simple {@link ISecureRandomSupplier} that holds a {@code SecureRandom} using
 * {@code DRBG} (Deterministic Random Bit Generator)
 * algorithm as defined by NIST SP 800‑90A and available in Java 9 and later.
 * If {@code DRBG} is not available on the running JVM, it falls back to
 * {@link SecureRandom#getInstanceStrong()}, which returns the strongest
 * SecureRandom implementation provided by the platform.
 * 
 * @author papegaaij
 */
public class DefaultSecureRandomSupplier implements ISecureRandomSupplier
{
    private static final class Holder
	{
		private static final SecureRandom INSTANCE;

		static
		{
            SecureRandom secureRandom;
			try
			{
				secureRandom = SecureRandom.getInstance("DRBG");
			} catch (NoSuchAlgorithmException e1) {
                try {
                    secureRandom = SecureRandom.getInstanceStrong();
                } catch (NoSuchAlgorithmException e2) {
                    throw new WicketRuntimeException("Critical security initialization failure: no suitable SecureRandom implementation found. " +
                                    "The application attempted to initialize 'DRBG' and 'SecureRandom.getInstanceStrong()', " +
                                    "but neither is available in the current JVM environment. ", e2);
                }
            }
            INSTANCE = secureRandom;
		}
	}

	@Override
	public SecureRandom getRandom()
	{
		return Holder.INSTANCE;
	}
}
