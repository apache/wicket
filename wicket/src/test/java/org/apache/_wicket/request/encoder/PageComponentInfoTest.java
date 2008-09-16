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

import junit.framework.TestCase;

/**
 * 
 * @author Matej Knopp
 */
public class PageComponentInfoTest extends TestCase
{

	/**
	 * Construct.
	 */
	public PageComponentInfoTest()
	{
	}

	private void testPageInfoOnly(PageComponentInfo info, String pageMapName, Integer pageId,
		Integer versionNumber)
	{
		assertNull(info.getComponentInfo());
		assertNotNull(info.getPageInfo());

		assertEquals(pageMapName, info.getPageInfo().getPageMapName());
		assertEquals(pageId, info.getPageInfo().getPageId());
		assertEquals(versionNumber, info.getPageInfo().getVersionNumber());
	}

	private void testPageComponentInfo(PageComponentInfo info, String pageMapName, Integer pageId,
		Integer versionNumber, String listener, String componentPath)
	{
		assertNotNull(info.getComponentInfo());
		assertNotNull(info.getPageInfo());

		assertEquals(pageMapName, info.getPageInfo().getPageMapName());
		assertEquals(pageId, info.getPageInfo().getPageId());
		assertEquals(versionNumber, info.getPageInfo().getVersionNumber());
		
		assertEquals(listener, info.getComponentInfo().getListenerInterface());
		assertEquals(componentPath, info.getComponentInfo().getComponentPath());
	}

	/**
	 * 
	 */
	public void test1()
	{
		String s = "2-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, null, 2, 0, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test2()
	{
		String s = "2.4-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, null, 2, 4, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	public void test3()
	{
		String s = "pagemap.2.4-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "pagemap", 2, 4, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test4()
	{
		String s = "pagemap-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "pagemap", null, null, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test5()
	{
		String s = "123pagemap-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "123pagemap", null, null, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test6()
	{
		String s = "123pagemap-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "123pagemap", null, null, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test7()
	{
		String s = "123pagemap.2-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "123pagemap", 2, 0, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test8()
	{
		String s = ".123-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "123", null, null, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test9()
	{
		String s = "123.2.0-click-foo-bar-baz";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageComponentInfo(info, "123", 2, 0, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test10()
	{
		String s = "abc.2";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageInfoOnly(info, "abc", 2, 0);
		assertEquals(s, info.toString());
	}
}
