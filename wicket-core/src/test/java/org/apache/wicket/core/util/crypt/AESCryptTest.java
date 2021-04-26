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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.wicket.core.random.DefaultSecureRandomSupplier;
import org.apache.wicket.util.crypt.CipherUtils;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

public class AESCryptTest
{
	@Test
	public void encrypDecrypt() throws GeneralSecurityException
	{
		DefaultSecureRandomSupplier randomSupplier = new DefaultSecureRandomSupplier();
		
		SecretKey secretKey = CipherUtils.generatePBEKey(
			"myWeakPassword", "PBKDF2WithHmacSHA1", "AES", 
			randomSupplier.getRandomBytes(16), 65536, 256);
		
		AbstractJceCrypt crypt = new AESCrypt(secretKey, randomSupplier);

		String inputTest = "inputTest";
		String encrypted = crypt.encryptUrlSafe(inputTest);

		String japFlowerBirdsWindMoon = "花鳥風月";
		String encrypted2 = crypt.encryptUrlSafe(japFlowerBirdsWindMoon);

		String decrypted = crypt.decryptUrlSafe(encrypted);
		assertEquals(decrypted, inputTest);

		String decrypted2 = crypt.decryptUrlSafe(encrypted2);
		assertEquals(decrypted2, japFlowerBirdsWindMoon);
	}
}
