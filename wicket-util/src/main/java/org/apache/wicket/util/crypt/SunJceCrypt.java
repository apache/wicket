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
package org.apache.wicket.util.crypt;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


/**
 * Provide some simple means to encrypt and decrypt strings such as passwords. The whole
 * implementation is based around Sun's security providers and uses the <a
 * href="http://www.ietf.org/rfc/rfc2898.txt">PBEWithMD5AndDES</a> method to encrypt and decrypt the
 * data.
 * 
 * @author Juergen Donnerstag
 */
public class SunJceCrypt extends AbstractCrypt
{
	/**
	 * Iteration count used in combination with the salt to create the encryption key.
	 */
	private final static int COUNT = 17;

	/** Name of encryption method */
	private static final String CRYPT_METHOD = "PBEWithMD5AndDES";

	/** Salt */
	private final static byte[] salt = { (byte)0x15, (byte)0x8c, (byte)0xa3, (byte)0x4a,
			(byte)0x66, (byte)0x51, (byte)0x2a, (byte)0xbc };

	/**
	 * Constructor
	 */
	public SunJceCrypt()
	{
		if (Security.getProviders("Cipher." + CRYPT_METHOD).length > 0)
		{
			return; // we are good to go!
		}
		try
		{
			// Initialize and add a security provider required for encryption
			final Class<?> clazz = Class.forName("com.sun.crypto.provider.SunJCE");

			Security.addProvider((Provider)clazz.newInstance());
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Unable to load SunJCE service provider", ex);
		}
	}

	/**
	 * Crypts the given byte array
	 * 
	 * @param input
	 *            byte array to be crypted
	 * @param mode
	 *            crypt mode
	 * @return the input crypted. Null in case of an error
	 * @throws GeneralSecurityException
	 */
	@Override
	protected final byte[] crypt(final byte[] input, final int mode)
		throws GeneralSecurityException
	{
		SecretKey key = generateSecretKey();
		PBEParameterSpec spec = new PBEParameterSpec(salt, COUNT);
		Cipher ciph = Cipher.getInstance(CRYPT_METHOD);
		ciph.init(mode, key, spec);
		return ciph.doFinal(input);
	}

	/**
	 * Generate the de-/encryption key.
	 * <p>
	 * Note: if you don't provide your own encryption key, the implementation will use a default. Be
	 * aware that this is potential security risk. Thus make sure you always provide your own one.
	 * 
	 * @return secretKey the security key generated
	 * @throws NoSuchAlgorithmException
	 *             unable to find encryption algorithm specified
	 * @throws InvalidKeySpecException
	 *             invalid encryption key
	 */
	private SecretKey generateSecretKey() throws NoSuchAlgorithmException,
		InvalidKeySpecException
	{
		final PBEKeySpec spec = new PBEKeySpec(getKey().toCharArray());
		return SecretKeyFactory.getInstance(CRYPT_METHOD).generateSecret(spec);
	}
}
