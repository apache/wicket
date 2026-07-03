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

import org.apache.wicket.Application;
import org.apache.wicket.settings.SecuritySettings;

/**
 * Base {@link ICryptFactory} that builds a {@link SchemeCrypt} from a key supplied by the concrete
 * factory and the encryption scheme + decryption whitelist configured on the application's
 * {@link SecuritySettings}. Subclasses only provide the key source (see {@link #getKey()}).
 */
public abstract class AbstractCryptFactory implements ICryptFactory
{
	@Override
	public ICrypt newCrypt()
	{
		return createCrypt(getKey());
	}

	/**
	 * Builds an {@link ICrypt} for the given key using the application's crypto policy.
	 *
	 * @param key
	 *            the secret key to bind
	 * @return a new {@link SchemeCrypt}
	 */
	protected ICrypt createCrypt(SecretKey key)
	{
		SecuritySettings settings = Application.get().getSecuritySettings();
		SecureRandom random = settings.getRandomSupplier().getRandom();
		return new SchemeCrypt(key, random, settings.getCryptScheme(),
			settings.getWhitelistedCryptSchemes());
	}

	/**
	 * @return the secret key to use; the key source (per-session, global, ...) is the concrete
	 *         factory's responsibility
	 */
	protected abstract SecretKey getKey();
}
