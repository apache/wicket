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
package org.apache.wicket.util.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jcompagner
 */
public class PrependingStringBufferTest
{

	/**
	 * Test method for 'org.apache.wicket.util.string.PrependingStringBuffer.prepend(String)'
	 */
	@Test
	public void prepend()
	{
		PrependingStringBuffer psb = new PrependingStringBuffer();
		psb.prepend("test1");
		assertEquals("test1", psb.toString());
		psb.prepend("test2");
		psb.prepend("test3");
		psb.prepend("test4");
		assertEquals("test4test3test2test1", psb.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void largeBegin() throws Exception
	{
		PrependingStringBuffer psb = new PrependingStringBuffer("123456789012345678901234567890");
		assertEquals("123456789012345678901234567890", psb.toString());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void charPrepend() throws Exception
	{
		PrependingStringBuffer psb = new PrependingStringBuffer("234567890");
		psb.prepend('1');
		assertEquals("1234567890", psb.toString());
	}

	/**
	 * Test <a href=
	 * "http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Object.html#equals(java.lang.Object)"
	 * >reflexivity<a> of the equals implementation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void equalsReflexive() throws Exception
	{
		PrependingStringBuffer psb = new PrependingStringBuffer("foo");
		assertTrue(psb.equals(psb));
	}

	/**
	 * Test if implementation of hashCode() meets its <a
	 * href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Object.html#hashCode()">contract<a>.
	 * 
	 * @throws Exception
	 */
	@Test
	public void hash() throws Exception
	{
		PrependingStringBuffer foo = new PrependingStringBuffer("foo");
		PrependingStringBuffer bar = new PrependingStringBuffer("foo");
		assertTrue(foo.equals(bar));
		assertEquals(foo.hashCode(), bar.hashCode());
	}


}
