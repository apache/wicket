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
package org.apache.wicket.request.mapper.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Matej Knopp
 */
class ComponentInfoTest
{
	/**
	 * 
	 */
	@Test
	void test1()
	{
		String s = "-component-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("component:path", info.getComponentPath());
		assertNull(info.getBehaviorId());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test2()
	{
		String s = "component-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals(null, info);
	}

	/**
	 * 
	 */
	@Test
	void test3()
	{
		String s = ".-";
		ComponentInfo info = ComponentInfo.parse(s);
		// empty component path is allowed - listener invoked on page
		assertEquals("", info.getComponentPath());
	}

	/**
	 * 
	 */
	@Test
	void test4()
	{
		String s = "-";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test5()
	{
		String s = "abcd";
		assertEquals(null, ComponentInfo.parse(s));
	}

	/**
	 * 
	 */
	@Test
	void test6()
	{
		String s = "-compo~~nent-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("compo--nent:path", info.getComponentPath());
		assertNull(info.getBehaviorId());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test7()
	{
		String s = "-co~mpo~~nent-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("co-mpo--nent:path", info.getComponentPath());
		assertNull(info.getBehaviorId());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test8()
	{
		String s = ".12-component-path";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals("component:path", info.getComponentPath());
		assertEquals((Object)12, info.getBehaviorId());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test9()
	{
		String s = "4.-a-b";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals((Integer)4, info.getRenderCount());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void test10()
	{
		String s = "4.5-a-b";
		ComponentInfo info = ComponentInfo.parse(s);
		assertEquals((Integer)4, info.getRenderCount());
		assertEquals((Integer)5, info.getBehaviorId());

		assertEquals(s, info.toString());
	}

	/**
	 * 
	 */
	@Test
	void encodeDecode()
	{
		final Integer renderCount = 1;
		final String componentPath = "-nav-container-:-nav:1:link";
		final Integer behaviorId = null;

		ComponentInfo info = new ComponentInfo(renderCount, componentPath, behaviorId);

		final String encoded = info.toString();
		assertEquals("1.-~nav~container~-~nav-1-link", encoded);

		ComponentInfo decoded = ComponentInfo.parse(encoded);
		assertEquals(renderCount, decoded.getRenderCount());
		assertEquals(componentPath, decoded.getComponentPath());
		assertEquals(behaviorId, decoded.getBehaviorId());
	}
}
