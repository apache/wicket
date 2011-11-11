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
package org.apache.wicket.extensions.markup.html.tree;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * Try to move the c3 node to the c2 at the described tree model:
 * 
 * - c1
 * 
 * - c2
 * 
 * - - cc2
 * 
 * - c3
 * 
 * @see "http://issues.apache.org/jira/browse/WICKET-2888"
 */
public class MoveChildToParentNodeMarkedForRecreationTest extends WicketTestCase
{
	/**
	 * test()
	 */
	@Test
	public void test()
	{
		MoveChildToParentNodeMarkedForRecreationTestPage testPage = new MoveChildToParentNodeMarkedForRecreationTestPage();
		tester.startPage(testPage);
		tester.clickLink("moveC3ToC2");
		assertTrue(testPage.c2.isNodeChild(testPage.c3));
	}
}
