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
package org.apache.wicket.markup.html.image;


import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;


public class InlineImageTest extends WicketTestCase
{

	@Test
	public void inlineImageTest()
	{
		tester.startPage(InlineImageTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		Assert.assertTrue(
			"inline image is in html",
			lastResponseAsString.contains("<img wicket:id=\"inlineimage\" src=\"data:image/gif;base64,R0lGODlhQAHwAPf8AAAAAAwMDAsNABUZABUXBRIS"));
	}
}
