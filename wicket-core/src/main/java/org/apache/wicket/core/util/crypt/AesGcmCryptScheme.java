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

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.wicket.WicketRuntimeException;

/**
 * Authenticated encryption using JDK-native AES-256-GCM ({@code AES/GCM/NoPadding}).
 * <p>
 * This is the default scheme: it needs no extra dependencies and provides confidentiality and
 * integrity. The ciphertext layout is {@code nonce(12) || ciphertext || tag(16)}; the scheme
 * marker supplied by {@link SchemeCrypt} is authenticated as associated data.
 * <p>
 * A fresh random 96-bit nonce is generated per message. Because each key is used by a single
 * session (or a single application), the number of encryptions under one key stays far below the
 * NIST SP&nbsp;800-38D safe-usage bound for random nonces. Deployments that need nonce-misuse
 * resistance beyond that bound can switch to {@link AesGcmSivCryptScheme}.
 */
public class AesGcmCryptScheme implements ICryptScheme
{
	/** Stable marker id for this scheme. */
	public static final byte ID = 1;

	private static final int NONCE_LENGTH = 12;

	private static final int TAG_LENGTH_BITS = 128;

	/**
	 * @return the {@link Cipher} to use
	 * @throws GeneralSecurityException
	 *             if the cipher is unavailable
	 */
	protected Cipher getCipher() throws GeneralSecurityException
	{
		return Cipher.getInstance("AES/GCM/NoPadding");
	}

	@Override
	public byte id()
	{
		return ID;
	}

	@Override
	public byte[] encrypt(byte[] plaintext, SecretKey key, byte[] aad, SecureRandom random)
	{
		try
		{
			byte[] nonce = new byte[NONCE_LENGTH];
			random.nextBytes(nonce);

			Cipher cipher = getCipher();
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce),
				random);
			if (aad != null)
			{
				cipher.updateAAD(aad);
			}

			byte[] ciphertext = cipher.doFinal(plaintext);

			byte[] result = Arrays.copyOf(nonce, nonce.length + ciphertext.length);
			System.arraycopy(ciphertext, 0, result, nonce.length, ciphertext.length);
			return result;
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] aad)
	{
		try
		{
			if (ciphertext.length < NONCE_LENGTH)
			{
				return null;
			}

			byte[] nonce = Arrays.copyOfRange(ciphertext, 0, NONCE_LENGTH);

			Cipher cipher = getCipher();
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
			if (aad != null)
			{
				cipher.updateAAD(aad);
			}

			return cipher.doFinal(ciphertext, NONCE_LENGTH, ciphertext.length - NONCE_LENGTH);
		}
		catch (GeneralSecurityException ex)
		{
			// authentication failure or malformed input
			return null;
		}
	}
}
