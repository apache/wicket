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

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class SunJceCryptTest extends Assert
{
	/**
	 * Default encryption uses {@value org.apache.wicket.util.crypt.SunJceCrypt#DEFAULT_CRYPT_METHOD}
	 */
	@Test
	public void defaultEncryption() throws GeneralSecurityException
	{
		SunJceCrypt crypt = new SunJceCrypt();
		String input = "input";
		byte[] encrypted = crypt.crypt(input.getBytes(), Cipher.ENCRYPT_MODE);

		byte[] decrypted = crypt.crypt(encrypted, Cipher.DECRYPT_MODE);
		assertThat(new String(decrypted), is(equalTo(input)));
	}

	/**
	 * Uses <em>PBEWithMD5AndTripleDES</em> if unlimited cryptography is installed
	 */
	@Test
	public void customPBEEncryption() throws GeneralSecurityException
	{
		boolean unlimitedStrengthJurisdictionPolicyInstalled = isUnlimitedStrengthJurisdictionPolicyInstalled();
		Assume.assumeThat(unlimitedStrengthJurisdictionPolicyInstalled, is(true));

		SunJceCrypt crypt = new SunJceCrypt("PBEWithMD5AndTripleDES");
		String input = "input";
		byte[] encrypted = crypt.crypt(input.getBytes(), Cipher.ENCRYPT_MODE);

		byte[] decrypted = crypt.crypt(encrypted, Cipher.DECRYPT_MODE);
		assertThat(new String(decrypted), is(equalTo(input)));
	}

	/**
	 * Checks whether Oracle Unlimited Strenght Jurisdiction Policy is installed
	 * Based on http://stackoverflow.com/a/8607735
	 *
	 * @return {@code true} if Unlimited Strenght Jurisdiction Policy is installed
	 * @throws NoSuchAlgorithmException
	 */
	static boolean isUnlimitedStrengthJurisdictionPolicyInstalled() throws NoSuchAlgorithmException
	{
		return Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;
	}
}
