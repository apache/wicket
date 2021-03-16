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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;


/**
 * Base class for JCE based ICrypt implementations.
 * 
 */
public abstract class AbstractJceCrypt implements ICrypt
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(AbstractJceCrypt.class);

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
			return new String(decrypt(decoded), StandardCharsets.UTF_8);
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
		byte[] encrypted = encrypt(plainText.getBytes(StandardCharsets.UTF_8));
		Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		byte[] encoded = encoder.encode(encrypted);
		return new String(encoded, StandardCharsets.UTF_8);
	}

	/**
	 * Decrypts an encrypted byte array.
	 * 
	 * @param encrypted
	 *            byte array to decrypt
	 * @return the decrypted text
	 */
	abstract protected byte[] decrypt(final byte[] encrypted);


	/**
	 * Encrypts the given text into a byte array.
	 * 
	 * @param plainText
	 *            text to encrypt
	 * @return the string encrypted
	 * @throws GeneralSecurityException
	 */
	abstract protected byte[] encrypt(final byte[] plainBytes);


	@Override
	final public void setKey(String key)
	{
		throw new UnsupportedOperationException("This method has been deprecated in ICrypt and will be removed.");
	}
}
