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

import org.apache.wicket.core.random.ISecureRandomSupplier;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.lang.Args;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * AES based {@link ICrypt} encrypt and decrypt strings such as passwords or URL segments.
 * Based on http://stackoverflow.com/a/992413
 * 
 * @see ICrypt
 */
public class AESCrypt extends AbstractJceCrypt
{

	private final SecretKey secretKey;
	private final String algorithm;
	private ISecureRandomSupplier randomSupplier;

	
	/**
	 * Constructor
	 * 
	 * @param secretKey
	 *              The {@link SecretKey} to use to initialize the {@link Cipher}.
	 * @param randomSupplier
	 *              The {@link ISecureRandomSupplier} to use to generate random values.
	 */
	public AESCrypt(SecretKey secretKey, ISecureRandomSupplier randomSupplier)
	{
		this(secretKey, "AES/CBC/PKCS5Padding", randomSupplier);
	}

	/**
	 * Constructor
	 * 
	 * @param secretKey
	 *              The {@link SecretKey} to use to initialize the {@link Cipher}.
	 * @param algorithm
	 *              The cipher algorithm to use, for example "AES/CBC/PKCS5Padding".
	 * @param randomSupplier
	 *              The {@link ISecureRandomSupplier} to use to generate random values.
	 */
	public AESCrypt(SecretKey secretKey, String algorithm, 
		ISecureRandomSupplier randomSupplier)
	{
		Args.notNull(secretKey, "secretKey");
		Args.notNull(algorithm, "algorithm");
		Args.notNull(randomSupplier, "randomSupplier");
		
		this.secretKey = secretKey;
		this.algorithm = algorithm;
		this.randomSupplier = randomSupplier;
	}

	@Override
	protected byte[] decrypt(byte[] encrypted)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(algorithm);
			
			int ivSize = cipher.getBlockSize();
			byte[] iv = new byte[ivSize];
			byte[] ciphertext = new byte[encrypted.length - ivSize];
			
			System.arraycopy(encrypted, 0, iv, 0, ivSize);
			System.arraycopy(encrypted, ivSize, ciphertext, 0, ciphertext.length);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			return cipher.doFinal(ciphertext);
		}
		catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException 
			| InvalidKeyException | InvalidAlgorithmParameterException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected byte[] encrypt(byte[] plainBytes)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(algorithm);
			int ivSize = cipher.getBlockSize();
			byte[] iv = randomSupplier.getRandomBytes(ivSize);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

			byte[] ciphertext = cipher.doFinal(plainBytes);
			byte[] finalRes = new byte[ciphertext.length + ivSize];
			
			System.arraycopy(iv, 0, finalRes, 0, ivSize);
			System.arraycopy(ciphertext, 0, finalRes, ivSize, ciphertext.length);
			
			return finalRes;
		}
		catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException 
			| InvalidKeyException | InvalidAlgorithmParameterException e)
		{
			throw new RuntimeException(e);
		}

	}
}