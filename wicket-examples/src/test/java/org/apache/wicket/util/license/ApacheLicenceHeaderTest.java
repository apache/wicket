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
package org.apache.wicket.util.license;

import java.util.Arrays;

/**
 * Test that the license headers are in place in this project. The tests are run from
 * {@link ApacheLicenseHeaderTestCase}, but you can add project specific tests here if needed.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public class ApacheLicenceHeaderTest extends ApacheLicenseHeaderTestCase
{

	/**
	 * Construct.
	 */
	public ApacheLicenceHeaderTest()
	{
		// addHeaders = true;

		/*
		 * This is an example project. Therefore we'd rather not have license headers in html files,
		 * because it removes the focus away from what the example is about.
		 */
		htmlIgnore = Arrays.asList("src");

		/* same, same */
		velocityIgnore = Arrays.asList("src");

		/*
		 * Prototype, released under MIT. See NOTICE
		 */
		javaScriptIgnore.add("src/main/webapp/prototype.js");
		/*
		 * jQuery UI, released under MIT. See NOTICE
		 */
		javaScriptIgnore.add("src/main/webapp/jquery-ui-1.10.3.custom.js");
		/*
		 * Behaviour, released under BSD. See NOTICE
		 */
		javaScriptIgnore.add("src/main/java/org/apache/wicket/examples/preview/behaviour.js");
		/*
		 * DOJO is dual licensed. We use the BSD license. See NOTICE.
		 */
		javaScriptIgnore.add("src/main/java/org/apache/wicket/examples/preview/dojo.js");

		/**
		 * QUnit and jQuery used for tests - MIT licence. See NOTICE
		 */
		javaScriptIgnore.add("src/main/webapp/js-test/lib/qunit.js");
		javaScriptIgnore.add("src/main/webapp/js-test/lib/jquery.js");

		cssIgnore.add("src/main/webapp/js-test/lib/qunit.css");

		/*
		 * Part of an example
		 */
		xmlIgnore.add("src/main/java/org/apache/wicket/examples/compref/XmlPage.xml");
		xmlIgnore.add(".settings");

		/*
		 * Configuration files with no "intelligent" content
		 */
		propertiesIgnore.add("src/main/java/commons-logging.properties");
		propertiesIgnore.add("src/main/java/log4j.properties");

		xmlPrologIgnore = Arrays.asList("src");
	}
}
