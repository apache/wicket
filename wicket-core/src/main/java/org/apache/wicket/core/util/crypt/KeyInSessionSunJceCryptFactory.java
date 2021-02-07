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

import java.io.Serializable;
import java.security.Provider;
import java.security.Security;
import java.util.Random;
import java.util.UUID;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.crypt.SunJceCrypt;
import org.apache.wicket.util.lang.Args;

/**
 * Crypt factory that produces {@link SunJceCrypt} instances based on session-specific
 * encryption key. This allows each user to have his own encryption key, hardening against CSRF
 * attacks.
 * <br>
 * Note that the use of this crypt factory will result in an immediate creation of a session.
 *
 * @author igor.vaynberg
 */
public class KeyInSessionSunJceCryptFactory implements ICryptFactory
{
	/** metadata-key used to store crypt data in session metadata */
	private static final MetaDataKey<CryptData> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final String cryptMethod;

	/**
	 * Constructor using {@link javax.crypto.Cipher} {@value org.apache.wicket.util.crypt.SunJceCrypt#DEFAULT_CRYPT_METHOD}
	 */
	public KeyInSessionSunJceCryptFactory()
	{
		this(SunJceCrypt.DEFAULT_CRYPT_METHOD);
	}

	/**
	 * Constructor that uses a custom {@link javax.crypto.Cipher}
	 *
	 * @param cryptMethod
	 *              the name of the crypt method (cipher)
	 */
	public KeyInSessionSunJceCryptFactory(String cryptMethod)
	{
		this.cryptMethod = Args.notNull(cryptMethod, "Crypt method");

		final Provider[] providers = Security.getProviders("Cipher." + cryptMethod);
		if (providers == null || providers.length == 0)
		{
			try
			{
				// Initialize and add a security provider required for encryption
				final Class<?> clazz = Class.forName("com.sun.crypto.provider.SunJCE");

				final Provider provider = (Provider) clazz.getDeclaredConstructor().newInstance();
				Security.addProvider(provider);
			}
			catch (Exception ex)
			{
				throw new RuntimeException("Unable to load SunJCE service provider", ex);
			}
		}
	}

	@Override
	public ICrypt newCrypt()
	{
		Session session = Session.get();
		session.bind();

		// retrieve or generate encryption key from session
		CryptData data = session.getMetaData(KEY);
		if (data == null)
		{
			// generate new salt
			byte[] salt = SunJceCrypt.randomSalt();
			
			// generate new key
			String key = session.getId() + "." + UUID.randomUUID().toString();
			
			data = new CryptData(key, salt);
			session.setMetaData(KEY, data);
		}

		// build the crypt based on session key and salt
		SunJceCrypt crypt = new SunJceCrypt(cryptMethod, data.salt, 1000);
		crypt.setKey(data.key);
		
		return crypt;
	}

	/**
	 * @return the {@link org.apache.wicket.util.crypt.ICrypt} to use
	 * 
	 * @deprecated this method is no longer called
	 */
	protected ICrypt createCrypt()
	{
		return null;
	}
	
	private static final class CryptData implements Serializable {
		final String key;
		
		final byte[] salt;
		
		CryptData(String key, byte[] salt) {
			this.key = key;
			this.salt = salt;
		}
	}
}
