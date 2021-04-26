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
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.wicket.util.lang.Args;


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
	private final static int DEFAULT_ITERATION_COUNT = 17;

	/** Name of the default encryption method */
	public static final String DEFAULT_CRYPT_METHOD = "PBEWithMD5AndDES";

	/**
	 * Default salt.
	 * 
	 * @deprecated TODO remove in Wicket 10
	 */
	@Deprecated
	public final static byte[] SALT = { (byte)0x15, (byte)0x8c, (byte)0xa3, (byte)0x4a,
			(byte)0x66, (byte)0x51, (byte)0x2a, (byte)0xbc };

	/** The name of encryption method (cipher) */
	private final String cryptMethod;
	
	private final int iterationCount;
	
	private final byte[] salt;
 
	/**
	 * Constructor
	 * 
	 * @deprecated TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	public SunJceCrypt()
	{
		this(DEFAULT_CRYPT_METHOD);
	}

	/**
	 * Constructor.
	 * 
	 * @param salt
	 *              salt for encryption
	 * @param iterationCount
	 * 				iteration count
	 */
	public SunJceCrypt(byte[] salt, int iterationCount)
	{
		this(DEFAULT_CRYPT_METHOD, salt, iterationCount);
	}

	/**
	 * Constructor
	 *
	 * @deprecated TODO remove in Wicket 10
	 */
	@Deprecated(forRemoval = true)
	public SunJceCrypt(String cryptMethod)
	{
		this(cryptMethod, SALT, DEFAULT_ITERATION_COUNT);
	}

	/**
	 * Constructor that uses a custom encryption method (cipher).
	 * You may need to override {@link #createKeySpec()} and/or
	 * {@link #createParameterSpec()} for the custom cipher.
	 *
	 * @param cryptMethod
	 *              the name of encryption method (the cipher)
	 * @param salt
	 *              salt for encryption
	 * @param iterationCount
	 * 				iteration count
	 */
	public SunJceCrypt(String cryptMethod, byte[] salt, int iterationCount)
	{
		this.cryptMethod = Args.notNull(cryptMethod, "Crypt method");
		this.salt = Args.notNull(salt, "salt");
		this.iterationCount = Args.withinRange(1, Integer.MAX_VALUE,  iterationCount, "iterationCount");
	}

	/**
	 * Crypts the given byte array
	 * 
	 * @param input
	 *            byte array to be encrypted
	 * @param mode
	 *            crypt mode
	 * @return the input crypted. Null in case of an error
	 * @throws GeneralSecurityException
	 */
	@Override
	protected byte[] crypt(final byte[] input, final int mode)
		throws GeneralSecurityException
	{
		SecretKey key = generateSecretKey();
		AlgorithmParameterSpec spec = createParameterSpec();
		Cipher ciph = createCipher(key, spec, mode);
		return ciph.doFinal(input);
	}

	/**
	 * Creates the {@link javax.crypto.Cipher} that will do the de-/encryption.
	 *
	 * @param key
	 *              the secret key to use
	 * @param spec
	 *              the parameters spec to use
	 * @param mode
	 *              the mode ({@link javax.crypto.Cipher#ENCRYPT_MODE} or {@link javax.crypto.Cipher#DECRYPT_MODE})
	 * @return the cipher that will do the de-/encryption
	 * @throws GeneralSecurityException
	 */
	protected Cipher createCipher(SecretKey key, AlgorithmParameterSpec spec, int mode) throws GeneralSecurityException
	{
		Cipher cipher = Cipher.getInstance(cryptMethod);
		cipher.init(mode, key, spec);
		return cipher;
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
	protected SecretKey generateSecretKey() throws NoSuchAlgorithmException,
		InvalidKeySpecException
	{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cryptMethod);
		KeySpec spec = createKeySpec();
		return keyFactory.generateSecret(spec);
	}
	
	/**
	 * @return the parameter spec to be used for the configured crypt method
	 */
	protected AlgorithmParameterSpec createParameterSpec()
	{
		return new PBEParameterSpec(salt, iterationCount);
	}

	/**
	 * @return the key spec to be used for the configured crypt method
	 */
	protected KeySpec createKeySpec()
	{
		return new PBEKeySpec(getKey().toCharArray());
	}

	/**
	 * Create a random salt to be used for this crypt. 
	 * 
	 * @return salt, always 8 bytes long
	 */
	public static byte[] randomSalt()
	{
		// must be 8 bytes - for anything else PBES1Core throws
		// InvalidAlgorithmParameterException: Salt must be 8 bytes long  
		byte[] salt = new byte[8];
		new Random().nextBytes(salt);
		return salt;
	}
}
