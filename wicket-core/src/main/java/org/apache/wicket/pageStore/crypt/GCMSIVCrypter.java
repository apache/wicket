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
package org.apache.wicket.pageStore.crypt;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.wicket.WicketRuntimeException;
import org.bouncycastle.jcajce.spec.AEADParameterSpec;

/**
 * Encryption and decryption implementation using AES-256-GCM-SIV authenticated encryption.
 * <p>
 * Because GCM-SIV is an AEAD mode, this implementation provides integrity as well as
 * confidentiality: modified or substituted ciphertext fails to decrypt instead of being handed to
 * the deserializer. It is therefore the implementation to choose when the page store needs to be
 * tamper-evident and not merely unreadable - unlike the unauthenticated {@link DefaultCrypter},
 * which is the default. Note that the key is held in the session, so this protects against parties
 * who can read or write the stored bytes, not against one who already controls the session.
 * <p>
 * This implementation requires Bouncy Castle, which is an optional dependency of {@code
 * wicket-core} and must be added explicitly. It is more secure than the {@link DefaultCrypter}, but
 * also more expensive. Simple measurements have shown {@link DefaultCrypter} to be about 10 to
 * 15 times faster than this implementation. This is likely caused by not-so-optimal implementation
 * of the algorithm in Java by BC. When the JDK gets support for GCM-SIV
 * (https://bugs.openjdk.org/browse/JDK-8256530), this implementation will likely be faster than or
 * about as fast as CBC.
 *
 * @see DefaultCrypter
 * @see ICrypter
 */
public class GCMSIVCrypter implements ICrypter
{
	protected Cipher getCipher() throws GeneralSecurityException
	{
		return Cipher.getInstance("AES/GCM-SIV/NoPadding");
	}

	@Override
	public SecretKey generateKey(SecureRandom random)
	{
		try
		{
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(256, random);
			return generator.generateKey();
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	@Override
	public byte[] encrypt(byte[] decrypted, SecretKey key, SecureRandom random)
	{
		try
		{
			Cipher cipher = getCipher();
			cipher.init(Cipher.ENCRYPT_MODE, key, random);

			AlgorithmParameters params = cipher.getParameters();
			byte[] nonce = params.getParameterSpec(AEADParameterSpec.class).getNonce();
			byte[] ciphertext = cipher.doFinal(decrypted);

			byte[] encrypted = Arrays.copyOf(nonce, nonce.length + ciphertext.length);
			System.arraycopy(ciphertext, 0, encrypted, nonce.length, ciphertext.length);

			return encrypted;
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}

	@Override
	public byte[] decrypt(byte[] encrypted, SecretKey key)
	{
		try
		{
			byte[] nonce = new byte[12];
			byte[] ciphertext = new byte[encrypted.length - nonce.length];
			System.arraycopy(encrypted, 0, nonce, 0, nonce.length);
			System.arraycopy(encrypted, nonce.length, ciphertext, 0, ciphertext.length);

			Cipher cipher = getCipher();
			cipher.init(Cipher.DECRYPT_MODE, key, new AEADParameterSpec(nonce, 128));
			byte[] decrypted = cipher.doFinal(ciphertext);

			return decrypted;
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}
}
