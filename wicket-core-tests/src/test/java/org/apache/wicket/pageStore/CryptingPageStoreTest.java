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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StreamCorruptedException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;

import org.apache.wicket.MockPage;
import org.apache.wicket.mock.MockPageContext;
import org.apache.wicket.mock.MockPageStore;
import org.apache.wicket.pageStore.crypt.DefaultCrypter;
import org.apache.wicket.pageStore.crypt.GCMSIVCrypter;
import org.apache.wicket.pageStore.crypt.ICrypter;
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
	
	static List<ICrypter> crypters()
	{
		return List.of(new DefaultCrypter(), new GCMSIVCrypter());
	}

	@ParameterizedTest
	@MethodSource("crypters")
	void test(ICrypter crypter)
	{
		CryptingPageStore store = buildPageStore(crypter);
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
	@MethodSource("crypters")
	void testFail(ICrypter crypter)
	{
		CryptingPageStore store = buildPageStore(crypter);
		JavaSerializer serializer = new JavaSerializer("test");

		MockPageContext context = new MockPageContext();

		int p = 42;

		MockPage add = new MockPage(p);
		SerializedPage serializedAdd = new SerializedPage(p, "foo", serializer.serialize(add));
		store.addPage(context, serializedAdd);

		// remove key from session
		context.clearSession();

		try
		{
			SerializedPage serializedGot = (SerializedPage) store.getPage(context, p);

			MockPage got = (MockPage) serializer.deserialize(serializedGot.getData());
			assertEquals(p, got.getPageId());
		}
		catch (RuntimeException ex)
		{
			assertTrue(
				ex.getCause() instanceof GeneralSecurityException
					|| ex.getCause() instanceof StreamCorruptedException,
				"unable to decrypt with new key");
		}
	}
	
	private CryptingPageStore buildPageStore(ICrypter crypter)
	{
		return new CryptingPageStore(new MockPageStore(), tester.getApplication())
		{
			@Override
			protected ICrypter newCrypter()
			{
				return crypter;
			}
		};
	}
}
