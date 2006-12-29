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
public class ApacheLicenseHeaderTest extends ApacheLicenseHeaderTestCase
{
	/**
	 * Define the files which should not be checked by the license header test.
	 */
	public ApacheLicenseHeaderTest()
	{
//		addHeaders = true;
		
		javaIgnore = new String[] {
				"src/main/java/wicket/util/diff/AddDelta.java",
				"src/main/java/wicket/util/diff/ChangeDelta.java",
				"src/main/java/wicket/util/diff/Chunk.java",
				"src/main/java/wicket/util/diff/DeleteDelta.java",
				"src/main/java/wicket/util/diff/Delta.java",
				"src/main/java/wicket/util/diff/Diff.java",
				"src/main/java/wicket/util/diff/DiffAlgorithm.java",
				"src/main/java/wicket/util/diff/DifferentiationFailedException.java",
				"src/main/java/wicket/util/diff/DiffException.java",
				"src/main/java/wicket/util/diff/PatchFailedException.java",
				"src/main/java/wicket/util/diff/Revision.java",
				"src/main/java/wicket/util/diff/RevisionVisitor.java",
				"src/main/java/wicket/util/diff/ToString.java",
				"src/main/java/wicket/util/diff/myers/DiffNode.java",
				"src/main/java/wicket/util/diff/myers/MyersDiff.java",
				"src/main/java/wicket/util/diff/myers/PathNode.java",
				"src/main/java/wicket/util/diff/myers/Snake.java",
		};
		
		cssIgnore = new String[] { "src/test/java/wicket/protocol/http/portlet/style.css",
				"src/test/java/wicket/ajax/mockStyleSheet3.css",
				"src/test/java/wicket/markup/parser/filter/style.css",
				"src/test/java/wicket/markup/parser/filter/test.css",
				"src/test/java/wicket/markup/parser/filter/sub/cborder.css",
				"src/test/java/wicket/markup/html/link/test3_de_DE.css",
				"src/test/java/wicket/markup/html/link/test.css",
				"src/test/java/wicket/markup/html/link/test2_myStyle.css"
		};

		xmlIgnore = new String[] { "EclipseCodeFormat.xml",
				"src/test/java/wicket/performance/results.xml",
				"src/assembly/bin.xml"
		};

		propertiesIgnore = new String[] { 
				"src/test/java/wicket" 
		};

		javaScriptIgnore = new String[] { "src/test/java/wicket/markup/parser/filter/test.js",
				"src/test/java/wicket/markup/html/packaged3.js",
				"src/test/java/wicket/markup/html/packaged4.js",
				"src/site/xdoc/onestat.js",
				/*
				 * See NOTICE.txt
				 */
				"src/main/java/wicket/ajax/wicket-ajax-debug-drag.js"
		};
		
		htmlIgnore = new String[] {
				"src/test/java/wicket",
				"src/main/java/wicket/util/diff/package.html",
				"src/main/java/wicket/util/diff/myers/package.html"
		};
	}
}
