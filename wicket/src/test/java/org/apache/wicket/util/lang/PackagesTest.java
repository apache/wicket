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
package org.apache.wicket.util.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * @since 1.4.20
 */
public class PackagesTest extends Assert
{
	@Test
	public void absolutePath1() throws Exception
	{
		String packageName = "org.apache.wicket.util.tester";
		String relativePath = "org/apache/wicket/util/tester/BlockedResourceLinkPage.html";

		String absolutePath = Packages.absolutePath(packageName, relativePath);
		assertEquals(relativePath, absolutePath);
	}

	@Test
	public void absolutePath2() throws Exception
	{
		String packageName = "org.apache.wicket.util";
		String relativePath = "tester/BlockedResourceLinkPage.html";

		String absolutePath = Packages.absolutePath(packageName, relativePath);
		assertEquals("org/apache/wicket/util/tester/BlockedResourceLinkPage.html", absolutePath);
	}

	@Test
	public void absolutePath3() throws Exception
	{
		String packageName = "org.apache.wicket.util";
		String relativePath = "wicket/BlockedResourceLinkPage.html";

		String absolutePath = Packages.absolutePath(packageName, relativePath);
		assertEquals("org/apache/wicket/util/wicket/BlockedResourceLinkPage.html", absolutePath);
	}

	@Test
	public void absolutePath4() throws Exception
	{
		String packageName = "org.apache.wicket.util";
		String relativePath = "../../BlockedResourceLinkPage.html";

		String absolutePath = Packages.absolutePath(packageName, relativePath);
		assertEquals("org/apache/BlockedResourceLinkPage.html", absolutePath);
	}
}
