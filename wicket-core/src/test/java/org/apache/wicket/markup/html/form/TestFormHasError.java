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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 */
public class TestFormHasError extends WicketTestCase
{

	/**
	 * testFormHasError()
	 */
	@Test
	public void testFormHasError()
	{
		tester.startPage(FormHasErrorPage.class);
		tester.assertRenderedPage(FormHasErrorPage.class);
		tester.clickLink("form:submitForm");
		tester.dumpPage();
	}

	/**
	 * testFormComponentHasError()
	 */
	@Test
	public void testFormComponentHasError()
	{
		tester.startPage(FormHasErrorPage.class);
		tester.assertRenderedPage(FormHasErrorPage.class);
		tester.clickLink("form:submitFormComponent");
		tester.dumpPage();
	}

	/**
	 * testComponentHasError()
	 */
	@Test
	public void testComponentHasError()
	{
		tester.startPage(FormHasErrorPage.class);
		tester.assertRenderedPage(FormHasErrorPage.class);
		tester.clickLink("form:submitComponent");
		tester.dumpPage();
	}
}
