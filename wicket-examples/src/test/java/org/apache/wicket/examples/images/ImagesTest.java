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
package org.apache.wicket.examples.images;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * jWebUnit test for Hello World.
 */
public class ImagesTest
{
	/**
	 * Test page.
	 *
     */
	@Test
	public void testImages() {
		WicketTester tester = new WicketTester(new ImagesApplication());
		try
		{
			tester.startPage(Home.class);
			tester.assertContains("Wicket Examples - images");
		}
		finally
		{
			tester.destroy();
		}
	}
}
