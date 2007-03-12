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
package wicket.markup.html.basic;

import wicket.WicketTestCase;

/**
 * Test for Label components
 * 
 * @author Eelco Hillenius
 */
public class LabelTest extends WicketTestCase
{
	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public LabelTest(String name)
	{
		super(name);
	}

	/**
	 * Test escaping markup.
	 * 
	 * @throws Exception
	 */
	public void testLabelWithEscapeMarkup() throws Exception
	{
		executeTest(LabelWithEscapeMarkupPage.class, "LabelWithEscapeMarkupPageExpectedResult.html");
	}

	/**
	 * Test not escaping markup.
	 * 
	 * @throws Exception
	 */
	public void testLabelWithoutEscapeMarkup() throws Exception
	{
		executeTest(LabelWithoutEscapeMarkupPage.class,
				"LabelWithoutEscapeMarkupPageExpectedResult.html");
	}
}
