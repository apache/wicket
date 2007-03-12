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
package wicket.util.crypt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import junit.framework.TestCase;
import wicket.settings.ISecuritySettings;

/**
 * 
 * @author Juergen Donnerstag
 */
public class Base64Test extends TestCase
{
	/**
	 * Construct.
	 * @param name
	 */
	public Base64Test(String name)
	{
		super(name);
	}
	
	/**
	 * @throws IOException
	 */
	public void test_1() throws IOException
	{
		for (int i=0; i < 200; i++)
		{
		    byte bytes1[] = new byte[200];
		    new Random().nextBytes(bytes1);
	
		    byte[] s = new Base64().encode(bytes1);
	
		    byte[] bytes2 = new Base64().decode(s);
		    boolean isEqual = ByteBuffer.wrap(bytes1).equals(ByteBuffer.wrap(bytes2) );
		    assertEquals(true, isEqual);
		}
	}

	/**
	 * @throws IOException
	 */
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
	public void test_2() throws IOException
	{
		String input = "This is a text which is longer than 76 character and which contains some none-ascii chars like these: �����?�`=";
	    String s = new NoCrypt().encrypt(input);

	    String output = new NoCrypt().decrypt(s);
	    assertEquals(input, output);
	}
	
	/**
	 * @throws IOException
	 */
	public void test_3() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";
		ICrypt crypt = new CachingSunJceCryptFactory(ISecuritySettings.DEFAULT_ENCRYPTION_KEY).newCrypt();
	    String s = crypt.encrypt(input);

	    String output = crypt.decrypt(s);
	    assertEquals(input, output);
	}
	/**
	 * @throws IOException
	 */
	public void test_UrlSafe_1() throws IOException
	{
		for (int i=0; i < 200; i++)
		{
		    byte bytes1[] = new byte[200];
		    new Random().nextBytes(bytes1);
	
		    byte[] s = new Base64UrlSafe().encode(bytes1);
	
		    byte[] bytes2 = new Base64UrlSafe().decode(s);
		    boolean isEqual = ByteBuffer.wrap(bytes1).equals(ByteBuffer.wrap(bytes2) );
		    assertEquals(true, isEqual);
		}
	}

	/**
	 * @throws IOException
	 */
	public void test_UrlSafe_1a() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";

	    byte[] s = new Base64UrlSafe().encode(input.getBytes());

	    byte[] bytes2 = new Base64UrlSafe().decode(s);
	    String output = new String(bytes2);
	    boolean isEqual = input.equals(output);
	    assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	public void test_UrlSafe_1b() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerA";

	    byte[] s = new Base64UrlSafe().encode(input.getBytes());

	    byte[] bytes2 = new Base64UrlSafe().decode(s);
	    String output = new String(bytes2);
	    boolean isEqual = input.equals(output);
	    assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	public void test_UrlSafe_1c() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerAB";

	    byte[] s = new Base64UrlSafe().encode(input.getBytes());

	    byte[] bytes2 = new Base64UrlSafe().decode(s);
	    String output = new String(bytes2);
	    boolean isEqual = input.equals(output);
	    assertEquals(true, isEqual);
	}

	/**
	 * @throws IOException
	 */
	public void test_UrlSafe_1d() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListenerABC";

	    byte[] s = new Base64UrlSafe().encode(input.getBytes());

	    byte[] bytes2 = new Base64UrlSafe().decode(s);
	    String output = new String(bytes2);
	    boolean isEqual = input.equals(output);
	    assertEquals(true, isEqual);
	}
	
	/**
	 * @throws IOException
	 */
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
	public void test_UrlSafe_3() throws IOException
	{
		String input = "wicket:interface=:2:entityTree:node:node:0:node:nodeLink::IBehaviorListener";
		ICrypt crypt = new CachingSunJceCryptFactory(ISecuritySettings.DEFAULT_ENCRYPTION_KEY).newCrypt();
	    String s = crypt.encryptUrlSafe(input);

	    String output = crypt.decryptUrlSafe(s);
	    assertEquals(input, output);
	}
}
