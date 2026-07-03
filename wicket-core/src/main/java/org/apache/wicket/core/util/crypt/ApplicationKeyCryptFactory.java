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
package org.apache.wicket.core.util.crypt;

import java.security.SecureRandom;

import javax.crypto.SecretKey;

import org.apache.wicket.util.lang.Args;

/**
 * An {@link ICryptFactory} using a single, application-wide key. Unlike
 * {@link KeyInSessionCryptFactory} the key is not tied to a session, so data encrypted with it can
 * be decrypted across sessions &mdash; required for things like a "remember me" cookie.
 * <p>
 * Use {@link #ApplicationKeyCryptFactory(SecretKey)} with a stable, externally-managed key to keep
 * data decryptable across application restarts. The
 * {@link #ApplicationKeyCryptFactory(SecureRandom)} constructor instead generates a key (for the
 * application's configured {@link org.apache.wicket.settings.SecuritySettings#getCryptScheme()
 * scheme}) on first use; that key lives only for the lifetime of this factory, so data does not
 * survive a restart.
 */
public class ApplicationKeyCryptFactory extends AbstractCryptFactory
{
	private final SecureRandom random;

	private SecretKey key;

	/**
	 * @param key
	 *            the application-wide key to use
	 */
	public ApplicationKeyCryptFactory(SecretKey key)
	{
		this.key = Args.notNull(key, "key");
		this.random = null;
	}

	/**
	 * Generates a random application-wide key on first use. Data encrypted with it does not survive
	 * a restart.
	 *
	 * @param random
	 *            source of randomness
	 */
	public ApplicationKeyCryptFactory(SecureRandom random)
	{
		this.random = Args.notNull(random, "random");
	}

	@Override
	protected synchronized SecretKey getKey()
	{
		if (key == null)
		{
			// generate the application-wide key lazily, once the scheme has been configured
			key = generateKey(random);
		}
		return key;
	}
}
