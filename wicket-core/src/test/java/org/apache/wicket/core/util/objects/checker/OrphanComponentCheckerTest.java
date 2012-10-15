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
package org.apache.wicket.core.util.objects.checker;

import org.apache.wicket.TestPage_1;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;

/**
 * Tests for OrphanComponentChecker
 */
public class OrphanComponentCheckerTest extends WicketTestCase
{
	@Test
	public void checkOrphanComponent()
	{
		WebComponent component = new WebComponent("a");
		IObjectChecker checker = new OrphanComponentChecker();
		IObjectChecker.Result result = checker.check(component);
		assertEquals(IObjectChecker.Result.Status.FAILURE, result.status);
		assertEquals("A component without a parent is detected.", result.reason);

		WebPage parent = new TestPage_1();
		parent.add(component);
		IObjectChecker.Result result2 = checker.check(component);
		assertEquals(IObjectChecker.Result.SUCCESS, result2);
	}
}
