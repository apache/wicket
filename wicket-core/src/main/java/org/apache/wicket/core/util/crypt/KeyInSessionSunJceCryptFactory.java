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

import org.apache.wicket.Session;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory.CryptData;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.SunJceCrypt;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Args;

import java.security.Provider;
import java.security.Security;
import java.util.UUID;

/**
 * Crypt factory that produces {@link SunJceCrypt} instances based on session-specific
 * encryption key. This allows each user to have his own encryption key, hardening against CSRF
 * attacks.
 * <br>
 * Note that the use of this crypt factory will result in an immediate creation of a session.
 *
 * @author igor.vaynberg
 */
public class KeyInSessionSunJceCryptFactory extends AbstractKeyInSessionCryptFactory<CryptData>
{

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

	/**
	 * @return the {@link org.apache.wicket.util.crypt.ICrypt} to use
	 * 
	 * @deprecated this method is no longer called TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	protected ICrypt createCrypt()
	{
		return null;
	}
	
	@Override
	protected CryptData generateKey(Session session)
	{
	    // generate new salt
        byte[] salt = SunJceCrypt.randomSalt();
        
	    // generate new key
        String key = session.getId() + "." + UUID.randomUUID().toString();
        
        return new CryptData(key, salt);
	}
	
	@Override
	protected ICrypt createCrypt(CryptData keyParams)
	{
	    SunJceCrypt crypt = new SunJceCrypt(cryptMethod, keyParams.salt, 1000);
        crypt.setKey(keyParams.key);
        
        return crypt;
	}

    static final class CryptData implements IClusterable
	{
        private static final long serialVersionUID = 1L;

        final String key;
		
		final byte[] salt;
		
		CryptData(String key, byte[] salt)
		{
			this.key = key;
			this.salt = salt;
		}
	}
}
