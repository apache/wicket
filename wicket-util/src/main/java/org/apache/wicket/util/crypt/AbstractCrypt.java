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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class for JCE based ICrypt implementations.
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractCrypt implements ICrypt
{
	/** Encoding used to convert java String from and to byte[] */
	private static final String CHARACTER_ENCODING = "UTF-8";

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(AbstractCrypt.class);

	/** Key used to de-/encrypt the data */
	private String encryptionKey;

	/**
	 * Constructor
	 */
	public AbstractCrypt()
	{
		this.encryptionKey = UUID.randomUUID().toString();
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
			byte[] decoded = new Base64(true).decode(text);
			return new String(decryptByteArray(decoded), CHARACTER_ENCODING);
		}
		catch (Exception ex)
		{
			log.debug("Error decoding text: " + text, ex);
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
		try
		{
			byte[] encrypted = encryptStringToByteArray(plainText);
			Base64 base64 = new Base64(-1, null, true);
			byte[] encoded = base64.encode(encrypted);
			return new String(encoded, CHARACTER_ENCODING);
		}
		catch (GeneralSecurityException e)
		{
			log.error("Unable to encrypt text '" + plainText + "'", e);
			return null;
		}
		catch (UnsupportedEncodingException e)
		{
			log.error("Unable to encrypt text '" + plainText + "'", e);
			return null;
		}
	}

	/**
	 * Get encryption private key
	 * 
	 * @return encryption private key
	 */
	public String getKey()
	{
		return encryptionKey;
	}

	/**
	 * Set encryption private key
	 * 
	 * @param key
	 *            private key to make de-/encryption unique
	 */
	@Override
	public void setKey(final String key)
	{
		encryptionKey = key;
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
	protected abstract byte[] crypt(final byte[] input, final int mode)
		throws GeneralSecurityException;

	/**
	 * Decrypts an encrypted, but Base64 decoded byte array into a byte array.
	 * 
	 * @param encrypted
	 *            byte array to decrypt
	 * @return the decrypted text
	 */
	private byte[] decryptByteArray(final byte[] encrypted)
	{
		try
		{
			return crypt(encrypted, Cipher.DECRYPT_MODE);
		}
		catch (GeneralSecurityException e)
		{
			throw new RuntimeException(
				"Unable to decrypt the text '" + new String(encrypted) + "'", e);
		}
	}

	/**
	 * Encrypts the given text into a byte array.
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return the string encrypted
	 * @throws GeneralSecurityException
	 */
	private byte[] encryptStringToByteArray(final String plainText)
		throws GeneralSecurityException
	{
		try
		{
			return crypt(plainText.getBytes(CHARACTER_ENCODING), Cipher.ENCRYPT_MODE);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new RuntimeException(ex.getMessage());
		}
	}
}
