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
package org.apache.wicket.ng.request.mapper.info;

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

	private void testPageInfoOnly(PageComponentInfo info, Integer pageId)
	{
		assertNull(info.getComponentInfo());
		assertNotNull(info.getPageInfo());

		assertEquals(pageId, info.getPageInfo().getPageId());
	}

	private void testPageComponentInfo(PageComponentInfo info, Integer pageId, String listener, String componentPath)
	{
		assertNotNull(info.getComponentInfo());
		assertNotNull(info.getPageInfo());

		assertEquals(pageId, info.getPageInfo().getPageId());
		
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
		testPageComponentInfo(info, 2, "click", "foo:bar:baz");
		assertEquals(s, info.toString());
	}
	
	/**
	 * 
	 */
	public void test2()
	{
		String s = "2";
		PageComponentInfo info = PageComponentInfo.parse(s);
		testPageInfoOnly(info, 2);
		assertEquals(s, info.toString());
	}
}
