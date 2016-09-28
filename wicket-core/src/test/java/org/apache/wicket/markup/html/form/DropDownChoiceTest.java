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

import java.util.Locale;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Juergen Donnerstag
 * @author svenmeier
 */
public class DropDownChoiceTest extends WicketTestCase
{

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		tester.getSession().setLocale(Locale.ENGLISH);
	}

	/**
	 * Null model object with null not valid.
	 * 
	 * @throws Exception
	 */
	@Test
	public void nullWithNullValidFalse() throws Exception
	{
		executeTest(new DropDownChoiceTestPage(null, false),
			"DropDownChoiceTestPage_null_false_expected.html");
	}

	/**
	 * Null model object with null valid.
	 * 
	 * @throws Exception
	 */
	@Test
	public void nullWithNullValidTrue() throws Exception
	{
		executeTest(new DropDownChoiceTestPage(null, true),
			"DropDownChoiceTestPage_null_true_expected.html");
	}

	/**
	 * "A" model object with null not valid.
	 * 
	 * @throws Exception
	 */
	@Test
	public void nonNullWithNullValidFalse() throws Exception
	{
		executeTest(new DropDownChoiceTestPage("A", false),
			"DropDownChoiceTestPage_A_false_expected.html");
	}

	/**
	 * "A" model object with null valid.
	 * 
	 * @throws Exception
	 */
	@Test
	public void nonNullWithNullValidTrue() throws Exception
	{
		executeTest(new DropDownChoiceTestPage("A", true),
			"DropDownChoiceTestPage_A_true_expected.html");
	}
}
