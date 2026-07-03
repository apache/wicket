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

import org.apache.wicket.WicketRuntimeException;
import org.bouncycastle.jcajce.spec.AEADParameterSpec;

/**
 * Authenticated encryption using AES-256-GCM-SIV ({@code AES/GCM-SIV/NoPadding}).
 * <p>
 * This scheme requires <a href="https://www.bouncycastle.org/">Bouncy Castle</a> (an optional
 * Wicket dependency) with its provider registered. Compared to {@link AesGcmCryptScheme} it adds
 * nonce-misuse resistance (safe even on accidental nonce reuse or beyond the random-nonce
 * usage bound), at the cost of the extra dependency and a slower software implementation until
 * the JDK ships native GCM-SIV (<a href="https://bugs.openjdk.org/browse/JDK-8256530">JDK-8256530</a>).
 * <p>
 * The ciphertext layout is {@code nonce(12) || ciphertext || tag(16)}; the scheme marker
 * supplied by {@link SchemeCrypt} is authenticated as associated data.
 */
public class AesGcmSivCryptScheme implements ICryptScheme
{
	/** Stable marker id for this scheme. */
	public static final byte ID = 2;

	private static final int NONCE_LENGTH = 12;

	private static final int TAG_LENGTH_BITS = 128;

	/**
	 * @return the {@link Cipher} to use
	 * @throws GeneralSecurityException
	 *             if the cipher is unavailable (e.g. Bouncy Castle not registered)
	 */
	protected Cipher getCipher() throws GeneralSecurityException
	{
		return Cipher.getInstance("AES/GCM-SIV/NoPadding");
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
			cipher.init(Cipher.ENCRYPT_MODE, key, new AEADParameterSpec(nonce, TAG_LENGTH_BITS),
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
			cipher.init(Cipher.DECRYPT_MODE, key, new AEADParameterSpec(nonce, TAG_LENGTH_BITS));
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
