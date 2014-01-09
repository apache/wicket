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
package org.apache.wicket.core.util.license;


import org.apache.wicket.util.license.ApacheLicenseHeaderTestCase;

/**
 * Test that the license headers are in place in this project. The tests are run from
 * {@link org.apache.wicket.util.license.ApacheLicenseHeaderTestCase}, but you can add project specific tests here if needed.
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
		 * See NOTICE.txt
		 */
		htmlIgnore.add("src/main/java/org/apache/wicket/util/diff");
		htmlIgnore.add("src/main/java/org/apache/wicket/markup/html/pages");
		htmlIgnore.add("src/main/java/org/apache/wicket/ajax/res/js/jquery");

		// the licence header breaks the tests in IE
		htmlIgnore.add("src/test/js/all.html");
		htmlIgnore.add("src/test/js/amd.html");

		/*
		 * See NOTICE.txt
		 */
		xmlPrologIgnore.add("src/main/java/org/apache/wicket/util/diff");
		// the xml prolog breaks the tests in IE
		xmlPrologIgnore.add("src/test/js/all.html");
		xmlPrologIgnore.add("src/test/js/amd.html");

		/*
		 * .css in test is very test specific and a license header would confuse and make it unclear
		 * what the test is about.
		 */
		cssIgnore.add("src/test/java");
		cssIgnore.add("src/test/js/qunit/qunit.css");

		xmlIgnore.add("src/assembly/bin.xml");

		/*
		 * ASL1.1. Taken from Maven JRCS. See NOTICE.txt
		 */
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/AddDelta.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/ChangeDelta.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/Chunk.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/DeleteDelta.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/Delta.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/Diff.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/DiffAlgorithm.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/DifferentiationFailedException.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/DiffException.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/PatchFailedException.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/Revision.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/RevisionVisitor.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/ToString.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/myers/DiffNode.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/myers/MyersDiff.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/myers/PathNode.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/diff/myers/Snake.java");
		/*
		 * Needs to be resolved (rewritten or NOTICE)
		 */
		javaIgnore.add("src/main/java/org/apache/wicket/util/concurrent/ConcurrentReaderHashMap.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/concurrent/ConcurrentHashMap.java");
		javaIgnore.add("src/main/java/org/apache/wicket/util/concurrent/CopyOnWriteArrayList.java");

		javaIgnore.add("src/main/java/org/apache/wicket/ajax/json");

		javaScriptIgnore.add("src/site/xdoc/onestat.js");

		/*
		 * .js in test is very test specific and a license header would confuse and make it unclear
		 * what the test is about.
		 */
		javaScriptIgnore.add("src/test/java");
		/*
		 * See NOTICE.txt
		 */
		javaScriptIgnore.add("src/main/java/org/apache/wicket/markup/html/form/upload/MultiFileUploadField.js");
		javaScriptIgnore.add("src/main/java/org/apache/wicket/resource/jquery");
		javaScriptIgnore.add("src/test/js/qunit/qunit.js");
		javaScriptIgnore.add("src/test/js/amd/require.js");
		javaScriptIgnore.add("src/test/js/data/ajax/nonWicketResponse.json"); // no way to add licence in JSON
	}
}
