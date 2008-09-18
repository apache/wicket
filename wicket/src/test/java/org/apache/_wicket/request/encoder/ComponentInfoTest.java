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
package org.apache._wicket.request.encoder;

import org.apache._wicket.request.encoder.info.ComponentInfo;

import junit.framework.TestCase;

/**
 * 
 * @author Matej Knopp
 */
public class ComponentInfoTest extends TestCase
{

	/**
	 * 
	 * Construct.
	 */
	public ComponentInfoTest()
	{
	}

	/**
	 * 
	 */
	public void test1()
	{
		String s = "listener-component-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("listener", info.getListenerInterface());
		assertEquals("component:path", info.getComponentPath());
		
		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	public void test2()
	{
		String s = "-component-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals(null, info.getListenerInterface());
		assertEquals("component:path", info.getComponentPath());
		
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test3()
	{
		String s = "listener-";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("listener", info.getListenerInterface());
		assertEquals(null, info.getComponentPath());
		
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test4()
	{
		String s = "-";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals(null, info.getListenerInterface());
		assertEquals(null, info.getComponentPath());
		
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test5()
	{
		String s = "abcd";
		assertEquals(null, ComponentInfo.parse(s));
	}
	
	/**
	 * 
	 */
	public void test6()
	{
		String s = "listener-compo--nent-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("listener", info.getListenerInterface());
		assertEquals("compo-nent:path", info.getComponentPath());
		
		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	public void test7()
	{
		String s = "listener-co--mpo----nent-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("listener", info.getListenerInterface());
		assertEquals("co-mpo--nent:path", info.getComponentPath());
		
		assertEquals(s, info.toString());
	}

}
