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
package org.apache.wicket.pageStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.security.Security;
import java.util.List;

import org.apache.wicket.MockPage;
import org.apache.wicket.core.util.crypt.AesGcmCryptScheme;
import org.apache.wicket.core.util.crypt.AesGcmSivCryptScheme;
import org.apache.wicket.core.util.crypt.ICryptScheme;
import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.mock.MockPageStore;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test for {@link CryptingPageStore}.
 *
 * @author svenmeier
 */
public class CryptingPageStoreTest extends WicketTestCase
{
	@BeforeAll
	public static void init()
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	static List<ICryptScheme> schemes()
	{
		return List.of(new AesGcmCryptScheme(), new AesGcmSivCryptScheme());
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void test(ICryptScheme scheme)
	{
		CryptingPageStore store = buildPageStore(scheme, new MockPageStore());
		JavaSerializer serializer = new JavaSerializer("test");

		IPageContext context = new MockPageContext();

		for (int p = 0; p < 10; p++)
		{
			MockPage add = new MockPage(p);
			SerializedPage serializedAdd = new SerializedPage(p, "foo", serializer.serialize(add));
			store.addPage(context, serializedAdd);

			SerializedPage serializedGot = (SerializedPage) store.getPage(context, p);
			MockPage got = (MockPage) serializer.deserialize(serializedGot.getData());
			assertEquals(p, got.getPageId());
		}
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void wrongKeyCannotDecrypt(ICryptScheme scheme)
	{
		CryptingPageStore store = buildPageStore(scheme, new MockPageStore());
		JavaSerializer serializer = new JavaSerializer("test");

		MockPageContext context = new MockPageContext();

		int p = 42;

		MockPage add = new MockPage(p);
		SerializedPage serializedAdd = new SerializedPage(p, "foo", serializer.serialize(add));
		store.addPage(context, serializedAdd);

		// remove key from session, so a new key will be generated on the next access
		context.clearSession();

		// the page can no longer be decrypted and is therefore treated as a cache miss
		assertNull(store.getPage(context, p));
	}

	@ParameterizedTest
	@MethodSource("schemes")
	void tamperedDataCannotDecrypt(ICryptScheme scheme)
	{
		MockPageStore delegate = new MockPageStore();
		CryptingPageStore store = buildPageStore(scheme, delegate);
		JavaSerializer serializer = new JavaSerializer("test");

		MockPageContext context = new MockPageContext();

		int p = 7;
		store.addPage(context, new SerializedPage(p, "foo", serializer.serialize(new MockPage(p))));

		// tamper with the stored ciphertext
		SerializedPage stored = (SerializedPage) delegate.getPage(context, p);
		byte[] data = stored.getData();
		data[data.length - 1] ^= 0x01;
		delegate.addPage(context, new SerializedPage(p, "foo", data));

		// authenticated encryption must detect the tampering; the page is treated as a cache miss
		assertNull(store.getPage(context, p));
	}

	private CryptingPageStore buildPageStore(ICryptScheme scheme, MockPageStore delegate)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setCryptScheme(scheme)
			.setWhitelistedCryptSchemes(List.of(scheme));
		return new CryptingPageStore(delegate, tester.getApplication());
	}
}
