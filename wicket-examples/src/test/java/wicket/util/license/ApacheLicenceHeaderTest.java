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

/**
 * Test that the license headers are in place in this project. The tests are run
 * from {@link ApacheLicenseHeaderTestCase}, but you can add project specific
 * tests here if needed.
 * 
 * @author Frank Bille Jensen (frankbille)
 */
public class ApacheLicenceHeaderTest extends ApacheLicenseHeaderTestCase {

	/**
	 * Construct.
	 */
	public ApacheLicenceHeaderTest() {

//		 addHeaders = true;

		htmlIgnore = new String[] {
				/*
				 * This is an example project. Therefore we'd rather not have
				 * license headers in html files, because it removes the
				 * focus away from what the example is about.
				 */
				"src"
		};
		
		javaScriptIgnore = new String[] {
				/*
				 * Prototype, released under MIT. See NOTICE
				 */
				"src/main/webapp/prototype.js",
				/*
				 * Script.aculo.us, released under MIT. See NOTICE
				 */
				"src/main/webapp/effects.js",
				"src/main/webapp/scriptaculous.js",
				/*
				 * Behaviour, released under BSD. See NOTICE
				 */
				"src/main/java/wicket/examples/preview/behaviour.js",
				/*
				 * DOJO???? Needs testing if it's legal to include.
				 */
				"src/main/java/wicket/examples/preview/dojo.js"
		};
		
		xmlIgnore = new String[] {
				/*
				 * Part of an example
				 */
				"src/main/java/wicket/examples/compref/XmlPage.xml"
		};
		
		propertiesIgnore = new String[] {
				/*
				 * Configuration files with no "intelligent" content
				 */
				"src/main/java/commons-logging.properties",
				"src/main/java/log4j.properties"
		};
		
		javaIgnore = new String[] {
				/*
				 * JettyHelper??? License ok???
				 */
				"src/test/java/nl/openedge/util/jetty",
				/*
				 * MIT style license. See NOTICE
				 */
				"src/test/java/com/meterware/httpunit/ParsedHTML.java"
		};
	}
}
