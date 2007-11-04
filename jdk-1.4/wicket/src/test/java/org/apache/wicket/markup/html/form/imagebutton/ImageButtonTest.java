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
package org.apache.wicket.markup.html.form.imagebutton;

import org.apache.wicket.WicketTestCase;

/**
 * @author Juergen Donnerstag
 */
public class ImageButtonTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public ImageButtonTest(String name)
	{
		super(name);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void test_1() throws Exception
	{
		tester.startPage(Home.class);

		tester.clickLink("goCanadian");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer_en_CA.gif\"");

		tester.clickLink("goChinese");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer_zh_CN.gif\"");

		tester.clickLink("goDanish");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer_da_DK.gif\"");

		tester.clickLink("goDutch");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer_nl_NL.gif\"");

		tester.clickLink("goGerman");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer_de_DE.gif\"");

		tester.clickLink("goUS");
		tester.assertContains("src=\"resources/org.apache.wicket.markup.html.form.imagebutton.Home/Beer.gif\"");
	}
}
