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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A demo how to create {@link org.apache.wicket.util.crypt.ICrypt} implementation that
 * uses <em>PBKDF2WithHmacSHA1</em> for encryption
 */
@SuppressWarnings("javadoc")
public class UnlimitedStrengthJurisdictionPolicyTest
{
	@Test
	public void unlimitedStrengthJurisdictionEncryption() throws GeneralSecurityException
	{
		boolean unlimitedStrengthJurisdictionPolicyInstalled = SunJceCryptTest.isUnlimitedStrengthJurisdictionPolicyInstalled();
		Assumptions.assumeTrue(unlimitedStrengthJurisdictionPolicyInstalled);

		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		
		GenericJceCrypt crypt = new UnlimitedStrenghtJurisdictionPolicyCrypt(buildKey("myWeakPassword"), ivParameterSpec);
		
		String input1 = "input1";
		String encrypted = crypt.encryptUrlSafe(input1);

		String input2 = "input2";
		String encrypted2 = crypt.encryptUrlSafe(input2);

		String decrypted = crypt.decryptUrlSafe(encrypted);
		assertEquals(decrypted, input1);

		String decrypted2 = crypt.decryptUrlSafe(encrypted2);
		assertEquals(decrypted2, input2);
	}

	/**
	 * Based on http://stackoverflow.com/a/992413
	 */
	private static class UnlimitedStrenghtJurisdictionPolicyCrypt extends GenericJceCrypt
	{
		
		private UnlimitedStrenghtJurisdictionPolicyCrypt(SecretKey secretKey, IvParameterSpec iv)
		{
		    super(secretKey, "AES/CBC/PKCS5Padding", iv);
		}
	}
	
	private static SecretKey buildKey(String password) 
	{
	    
	    try {
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        KeySpec spec = new PBEKeySpec(password.toCharArray(), SunJceCrypt.SALT, 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        return new SecretKeySpec(tmp.getEncoded(), "AES");
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
