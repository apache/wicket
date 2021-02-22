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
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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
		
		AbstractJceCrypt crypt = new UnlimitedStrenghtJurisdictionPolicyCrypt();
		
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
	private static class UnlimitedStrenghtJurisdictionPolicyCrypt extends AbstractJceCrypt
	{
		
		private SecretKey secretKey;

        private UnlimitedStrenghtJurisdictionPolicyCrypt()
		{
            this.secretKey = buildKey("myWeakPassword");
		}

        @Override
        protected byte[] decryptByteArray(byte[] encrypted)
        {
            byte[] iv = new byte[16];
            byte[] ciphertext = new byte[encrypted.length - 16];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            System.arraycopy(encrypted, 16, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = buildCipher(Cipher.DECRYPT_MODE, secretKey, 
                    "AES/CBC/PKCS5Padding", new IvParameterSpec(iv));
            
            try 
            {
                return cipher.doFinal(ciphertext);
            } catch (IllegalBlockSizeException | BadPaddingException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected byte[] encryptStringToByteArray(String plainText)
        {
            byte[] iv = generateIV();
            Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, secretKey, 
                    "AES/CBC/PKCS5Padding", new IvParameterSpec(iv));
            byte[] ciphertext;
            
            try 
            {
                ciphertext = cipher.doFinal(plainText.getBytes(CHARACTER_ENCODING));
            } catch (IllegalBlockSizeException | BadPaddingException e) 
            {
                throw new RuntimeException(e);
            }
            
            byte[] finalRes = new byte[ciphertext.length + iv.length];
            
            System.arraycopy(iv, 0, finalRes, 0, iv.length);
            System.arraycopy(ciphertext, 0, finalRes, iv.length, ciphertext.length);
            
            return finalRes;
        }
	}
	
	private static SecretKey buildKey(String password) 
	{
	    
	    try 
	    {
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        KeySpec spec = new PBEKeySpec(password.toCharArray(), SunJceCrypt.SALT, 65536, 256);
	        SecretKey tmp = factory.generateSecret(spec);
	        return new SecretKeySpec(tmp.getEncoded(), "AES");
	    } catch (Exception e) 
	    {
	        throw new RuntimeException(e);
	    }
	}
	
	private static byte[] generateIV() 
	{
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
