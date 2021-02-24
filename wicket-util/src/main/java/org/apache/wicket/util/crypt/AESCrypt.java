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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Based on http://stackoverflow.com/a/992413
 */
class AESCrypt extends AbstractJceCrypt
{

	private final SecretKey secretKey;
	private final String algorithm;
	private final int ivSize;

	
	public AESCrypt(SecretKey secretKey)
	{
		this(secretKey, "AES/CBC/PKCS5Padding", 16);
	}

	private AESCrypt(SecretKey secretKey, String algorithm, int ivSize)
	{
		this.secretKey = secretKey;
		this.algorithm = algorithm;
		this.ivSize = ivSize;
	}

	@Override
	protected byte[] decryptByteArray(byte[] encrypted)
	{
		byte[] iv = new byte[ivSize];
		byte[] ciphertext = new byte[encrypted.length - ivSize];
		System.arraycopy(encrypted, 0, iv, 0, ivSize);
		System.arraycopy(encrypted, ivSize, ciphertext, 0, ciphertext.length);

		Cipher cipher = buildCipher(Cipher.DECRYPT_MODE, secretKey, algorithm,
			new IvParameterSpec(iv));

		try
		{
			return cipher.doFinal(ciphertext);
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected byte[] encryptStringToByteArray(String plainText)
	{
		byte[] iv = CipherUtils.randomByteArray(ivSize);
		Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, secretKey, algorithm,
			new IvParameterSpec(iv));
		byte[] ciphertext;

		try
		{
			ciphertext = cipher.doFinal(plainText.getBytes(CHARACTER_ENCODING));
		}
		catch (IllegalBlockSizeException | BadPaddingException e)
		{
			throw new RuntimeException(e);
		}

		byte[] finalRes = new byte[ciphertext.length + ivSize];

		System.arraycopy(iv, 0, finalRes, 0, ivSize);
		System.arraycopy(ciphertext, 0, finalRes, ivSize, ciphertext.length);

		return finalRes;
	}
}