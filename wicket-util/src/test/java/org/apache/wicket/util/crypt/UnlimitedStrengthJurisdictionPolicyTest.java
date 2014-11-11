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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * A demo how to create {@link org.apache.wicket.util.crypt.ICrypt} implementation that
 * uses <em>PBKDF2WithHmacSHA1</em> for encryption
 */
public class UnlimitedStrengthJurisdictionPolicyTest extends Assert
{
	@Test
	public void unlimitedStrengthJurisdictionEncryption() throws GeneralSecurityException
	{
		boolean unlimitedStrengthJurisdictionPolicyInstalled = SunJceCryptTest.isUnlimitedStrengthJurisdictionPolicyInstalled();
		Assume.assumeThat(unlimitedStrengthJurisdictionPolicyInstalled, is(true));

		AbstractCrypt crypt = new UnlimitedStrenghtJurisdictionPolicyCrypt();

		String input1 = "input1";
		byte[] encrypted = crypt.crypt(input1.getBytes(), Cipher.ENCRYPT_MODE);

		String input2 = "input2";
		byte[] encrypted2 = crypt.crypt(input2.getBytes(), Cipher.ENCRYPT_MODE);

		byte[] decrypted = crypt.crypt(encrypted, Cipher.DECRYPT_MODE);
		assertThat(new String(decrypted), is(equalTo(input1)));

		byte[] decrypted2 = crypt.crypt(encrypted2, Cipher.DECRYPT_MODE);
		assertThat(new String(decrypted2), is(equalTo(input2)));
	}

	/**
	 * Based on http://stackoverflow.com/a/992413
	 */
	private static class UnlimitedStrenghtJurisdictionPolicyCrypt extends AbstractCrypt
	{
		private final Cipher crypter;
		private final Cipher decrypter;

		private UnlimitedStrenghtJurisdictionPolicyCrypt() throws GeneralSecurityException
		{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(getKey().toCharArray(), SunJceCrypt.SALT, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			String transformation = "AES/CBC/PKCS5Padding";
			crypter = Cipher.getInstance(transformation);
			crypter.init(Cipher.ENCRYPT_MODE, secret);
			AlgorithmParameters params = crypter.getParameters();
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

			decrypter = Cipher.getInstance(transformation);
			decrypter.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
		}

		@Override
		protected byte[] crypt(byte[] input, int mode) throws GeneralSecurityException
		{
			byte[] result;
			switch (mode)
			{
				case Cipher.ENCRYPT_MODE:
					result = crypter.doFinal(input);
					break;
				case Cipher.DECRYPT_MODE:
					result = decrypter.doFinal(input);
					break;
				default:
					throw new RuntimeException("Wrong crypt mode: " + mode);
			}
			return result;
		}
	}
}
