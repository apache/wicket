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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * 
 */
public class FragmentTest extends WicketTestCase
{
	@Test
	public void testFragments()
	{
		tester.startComponentInPage(FragmentTestPanel.class);
	}

	/**
	 * WICKET-5060
	 */
	@Test
	public void testComponentAndFragmentWithSameId()
	{
		try
		{
			tester.startComponentInPage(FragmentTestPanel_2.class);
			fail();
		}
		catch (MarkupNotFoundException ex)
		{
			assertTrue(ex.getMessage().contains("is not a <wicket:fragment> tag"));
		}
	}
}
