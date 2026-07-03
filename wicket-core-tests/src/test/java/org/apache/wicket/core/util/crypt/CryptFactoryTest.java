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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for the key-source {@link ICryptFactory factories}.
 */
class CryptFactoryTest extends WicketTestCase
{
	/**
	 * An application-wide key can decrypt data across separate {@link ICrypt} instances (as needed
	 * for e.g. a "remember me" cookie).
	 */
	@Test
	void applicationKeyIsStableAcrossCryptInstances()
	{
		SecretKey key = new SecretKeySpec(new byte[32], "AES");
		ApplicationKeyCryptFactory factory = new ApplicationKeyCryptFactory(key);

		byte[] encrypted = factory.newCrypt().encrypt("hello".getBytes(UTF_8));
		assertArrayEquals("hello".getBytes(UTF_8), factory.newCrypt().decrypt(encrypted));
	}

	/**
	 * The {@link ApplicationKeyCryptFactory#ApplicationKeyCryptFactory(java.security.SecureRandom)
	 * random-key} constructor generates its key lazily (via the configured scheme) and then keeps
	 * it stable across {@link ICrypt} instances.
	 */
	@Test
	void applicationRandomKeyIsStableAcrossCryptInstances()
	{
		ApplicationKeyCryptFactory factory = new ApplicationKeyCryptFactory(
			tester.getApplication().getSecuritySettings().getRandomSupplier().getRandom());

		byte[] encrypted = factory.newCrypt().encrypt("hello".getBytes(UTF_8));
		assertArrayEquals("hello".getBytes(UTF_8), factory.newCrypt().decrypt(encrypted));
	}

	/**
	 * The per-session key is stable within a session, so data encrypted through one {@link ICrypt}
	 * can be decrypted through another obtained from the same factory in the same session.
	 */
	@Test
	void keyInSessionIsStableWithinSession()
	{
		tester.getSession().bind();
		KeyInSessionCryptFactory factory = new KeyInSessionCryptFactory();

		byte[] encrypted = factory.newCrypt().encrypt("hello".getBytes(UTF_8));
		assertArrayEquals("hello".getBytes(UTF_8), factory.newCrypt().decrypt(encrypted));
	}
}
