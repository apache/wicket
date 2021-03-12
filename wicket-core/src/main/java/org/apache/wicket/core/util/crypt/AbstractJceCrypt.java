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

import org.apache.wicket.util.crypt.ICrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 * Base class for JCE based ICrypt implementations.
 * 
 */
public abstract class AbstractJceCrypt implements ICrypt
{
	/** Encoding used to convert java String from and to byte[] */
	public static final Charset CHARACTER_ENCODING = StandardCharsets.UTF_8;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(AbstractJceCrypt.class);

	protected Cipher buildCipher(int opMode, SecretKey secretKey, String transformation,
		AlgorithmParameterSpec params)
	{
		try
		{
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(opMode, secretKey, params);
			return cipher;
		}
		catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
			| NoSuchPaddingException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decrypts a string into a string.
	 * 
	 * @param text
	 *            text to decrypt
	 * @return the decrypted text
	 */
	@Override
	public final String decryptUrlSafe(final String text)
	{
		try
		{
			byte[] decoded = java.util.Base64.getUrlDecoder().decode(text);
			return new String(decryptByteArray(decoded), CHARACTER_ENCODING);
		}
		catch (Exception ex)
		{
			log.debug("Error decoding text: {}", text, ex);
			return null;
		}
	}

	/**
	 * Encrypt a string into a string using URL safe Base64 encoding.
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return encrypted string
	 */
	@Override
	public final String encryptUrlSafe(final String plainText)
	{
		byte[] encrypted = encryptStringToByteArray(plainText);
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		byte[] encoded = encoder.encode(encrypted);
		return new String(encoded, CHARACTER_ENCODING);
	}

	/**
	 * Decrypts an encrypted byte array.
	 * 
	 * @param encrypted
	 *            byte array to decrypt
	 * @return the decrypted text
	 */
	abstract protected byte[] decryptByteArray(final byte[] encrypted);


	/**
	 * Encrypts the given text into a byte array.
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return the string encrypted
	 * @throws GeneralSecurityException
	 */
	abstract protected byte[] encryptStringToByteArray(final String plainText);


	@Override
	final public void setKey(String key)
	{
		throw new UnsupportedOperationException();
	}
}
