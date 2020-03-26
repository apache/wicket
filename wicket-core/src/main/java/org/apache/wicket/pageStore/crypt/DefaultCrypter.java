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
import javax.crypto.spec.IvParameterSpec;

import org.apache.wicket.WicketRuntimeException;

/**
 * Default encryption and decryption implementation.
 */
public class DefaultCrypter implements ICrypter
{
	protected Cipher getCipher() throws GeneralSecurityException
	{
		return Cipher.getInstance("AES/CBC/PKCS5Padding");
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
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

			byte[] ciphertext = cipher.doFinal(decrypted);

			byte[] encrypted = Arrays.copyOf(iv, iv.length + ciphertext.length);
			System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);

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
			byte[] iv = new byte[16];
			byte[] ciphertext = new byte[encrypted.length - 16];
			System.arraycopy(encrypted, 0, iv, 0, iv.length);
			System.arraycopy(encrypted, 16, ciphertext, 0, ciphertext.length);

			Cipher cipher = getCipher();
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			byte[] decrypted = cipher.doFinal(ciphertext);

			return decrypted;
		}
		catch (GeneralSecurityException ex)
		{
			throw new WicketRuntimeException(ex);
		}
	}
}
