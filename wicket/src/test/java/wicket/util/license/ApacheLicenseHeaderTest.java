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
package wicket.util.license;

import wicket.util.license.ApacheLicenseHeaderTestCase;

/**
 * Test that the license headers are in place in this project. The tests are run
 * from {@link ApacheLicenseHeaderTestCase}, but you can add project specific
 * tests here if needed.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public class ApacheLicenseHeaderTest extends ApacheLicenseHeaderTestCase
{
	/**
	 * Define the files which should not be checked by the license header test.
	 */
	public ApacheLicenseHeaderTest()
	{
//		addHeaders = true;
		
		cssIgnore = new String[] { "src/test/java/wicket/protocol/http/portlet/style.css",
				"src/test/java/wicket/ajax/mockStyleSheet3.css",
				"src/test/java/wicket/markup/parser/filter/style.css",
				"src/test/java/wicket/markup/parser/filter/test.css",
				"src/test/java/wicket/markup/parser/filter/sub/cborder.css",
				"src/test/java/wicket/markup/html/link/test3_de_DE.css",
				"src/test/java/wicket/markup/html/link/test.css",
				"src/test/java/wicket/markup/html/link/test2_myStyle.css" };

		xmlIgnore = new String[] { "EclipseCodeFormat.xml",
				"src/test/java/wicket/performance/results.xml" };

		propertiesIgnore = new String[] { "src/test/java/wicket/MyMockPage.properties",
				"src/test/java/wicket/resource/DummyResources.properties",
				"src/test/java/wicket/resource/DummyPage.properties",
				"src/test/java/wicket/resource/DummySubClassPage.properties",
				"src/test/java/wicket/resource/DummyComponent_zz.properties",
				"src/test/java/wicket/resource/DummyApplication_zz.properties",
				"src/test/java/wicket/resource/DummyResources_zz.properties",
				"src/test/java/wicket/resource/DummyComponent.properties",
				"src/test/java/wicket/resource/DummyComponent_alt.properties",
				"src/test/java/wicket/resource/DummyApplication_alt.properties",
				"src/test/java/wicket/resource/DummyApplication.properties",
				"src/test/java/wicket/util/tester/apps_1/MyMockApplication_de.properties",
				"src/test/java/wicket/util/tester/apps_1/MyMockApplication_nl.properties",
				"src/test/java/wicket/util/tester/apps_1/CreateBook.properties",
				"src/test/java/wicket/util/tester/apps_1/MyMockApplication.properties",
				"src/test/java/wicket/util/tester/apps_4/EmailPage.properties",
				"src/test/java/wicket/markup/html/basic/SimplePage_7.properties",
				"src/test/java/wicket/model/StringResourceModelTest.properties",
				"src/test/java/wicket/properties/MyApplication.properties",
				"src/test/java/wicket/properties/MyApplication_de.properties",
				"src/test/java/wicket/properties/MyApplication_mystyle_de.properties",
				"src/test/java/wicket/properties/MyApplication_mystyle.properties",
				"src/test/java/wicket/properties/TestPage.properties",
				"src/test/java/wicket/properties/MyTesterApplication.properties",
				"src/test/java/wicket/properties/TestContainer.properties",
				"src/test/java/wicket/properties/TestForm.properties" };

		javaScriptIgnore = new String[] { "src/test/java/wicket/markup/parser/filter/test.js",
				"src/test/java/wicket/markup/html/packaged3.js",
				"src/test/java/wicket/markup/html/packaged4.js",
				"src/site/xdoc/onestat.js"};
	}
}
