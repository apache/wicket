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
		 * See NOTICE.txt
		 */
		htmlIgnore.add("src/main/java/org/apache/wicket/util/diff");

		xmlPrologIgnore.add("src/main/java/org/apache/wicket/util/diff");

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

		javaScriptIgnore.add("src/site/xdoc/onestat.js");

		/*
		 * .js in test is very test specific and a license header would confuse and make it unclear
		 * what the test is about.
		 */
		javaScriptIgnore.add("src/test/java");

		/*
		 * See NOTICE.txt
		 */
		javaScriptIgnore.add("src/main/java/org/apache/wicket/ajax/wicket-ajax-debug-drag.js");
		javaScriptIgnore.add("src/main/java/org/apache/wicket/markup/html/form/upload/MultiFileUploadField.js");
	}
}
