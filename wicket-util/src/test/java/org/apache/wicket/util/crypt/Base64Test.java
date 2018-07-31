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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 
 * @author Juergen Donnerstag
 */
public class Base64Test
{
	/**
	 * @throws IOException
	 */
	@Test
	public void test_1() throws IOException
	{
		for (int i = 0; i < 200; i++)
		{
			byte bytes1[] = new byte[200];
			new Random().nextBytes(bytes1);

			byte[] s = new Base64().encode(bytes1);

			byte[] bytes2 = new Base64().decode(s);
			boolean isEqual = ByteBuffer.wrap(bytes1).equals(ByteBuffer.wrap(bytes2));
			assertEquals(true, isEqual);
		}
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_1a() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_1b() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerA";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_1c() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerAB";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_1d() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerABC";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_2() throws IOException
	{
		String input = "This is a text which is longer than 76 character and which contains some none-ascii chars like these: �����?�`=";
		String s = new NoCrypt().encryptUrlSafe(input);

		String output = new NoCrypt().decryptUrlSafe(s);
		assertEquals(input, output);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_3() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";
		ICrypt crypt = new CachingSunJceCryptFactory("WiCkEt-FRAMEwork").newCrypt();
		String s = crypt.encryptUrlSafe(input);

		String output = crypt.decryptUrlSafe(s);
		assertEquals(input, output);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_1() throws IOException
	{
		for (int i = 0; i < 200; i++)
		{
			byte bytes1[] = new byte[200];
			new Random().nextBytes(bytes1);

			byte[] s = new Base64().encode(bytes1);

			byte[] bytes2 = new Base64().decode(s);
			boolean isEqual = ByteBuffer.wrap(bytes1).equals(ByteBuffer.wrap(bytes2));
			assertEquals(true, isEqual);
		}
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_1a() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_1b() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerA";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_1c() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerAB";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_1d() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerABC";

		byte[] s = new Base64().encode(input.getBytes());

		byte[] bytes2 = new Base64().decode(s);
		String output = new String(bytes2);
		boolean isEqual = input.equals(output);
		assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_UrlSafe_2() throws IOException
	{
		String input = "This is a text which is longer than 76 character and which contains some none-ascii chars like these: �����?�`=";
		String s = new NoCrypt().encryptUrlSafe(input);

		String output = new NoCrypt().decryptUrlSafe(s);
		assertEquals(input, output);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_4() throws IOException
	{
		String input = "wicket-sep-wicket";
		for (int i = input.length(); i >= 0; i--)
		{
			String in = input.substring(i);
			byte[] s = Base64.encodeBase64URLSafe(in.getBytes());

			byte[] output = Base64.decodeBase64(s);
			String out = new String(output);
			assertEquals(in, out);
		}
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void test_5() throws IOException
	{
		ICrypt crypt = new CachingSunJceCryptFactory("WiCkEt-FRAMEwork").newCrypt();

		String input = "wicket-sep-wicket";
		for (int i = input.length(); i >= 0; i--)
		{
			String in = input.substring(i);
			String encrypted = crypt.encryptUrlSafe(in);

			String output = crypt.decryptUrlSafe(encrypted);
			assertEquals(in, output);
		}
	}
}
