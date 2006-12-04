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
public class ApacheLicenceHeaderTest extends ApacheLicenseHeaderTestCase
{
	/**
	 * Construct.
	 */
	public ApacheLicenceHeaderTest()
	{
//		addHeaders = true;
		
		htmlIgnore = new String[] {
				/*
				 * .html in test is very test specific and a license header 
				 * would confuse and make it unclear what the test is about. 
				 */
				"src/test/java",
				/*
				 * See NOTICE.txt
				 */
				"src/main/java/wicket/util/diff",				
		};
		
		cssIgnore = new String[] {
				/*
				 * .css in test is very test specific and a license header 
				 * would confuse and make it unclear what the test is about. 
				 */
				"src/test/java" 
		};
		
		xmlIgnore = new String[] { 
				"EclipseCodeFormat.xml",
				"src/assembly/bin.xml"
		};
		
		javaIgnore = new String[] {
				/*
				 * ASL1.1. Taken from Maven JRCS. See NOTICE.txt
				 */
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
				/*
				 * Needs to be rewritten
				 */
				"src/main/java/wicket/protocol/http/ClientProperties.java",
				/*
				 * Needs to be resolved (rewritten or NOTICE)
				 */
				"src/main/java/wicket/util/concurrent/ConcurrentReaderHashMap.java",
				"src/main/java/wicket/util/concurrent/ConcurrentHashMap.java",
				"src/main/java/wicket/util/concurrent/CopyOnWriteArrayList.java"
		};
		
		javaScriptIgnore = new String[] { 
				"src/site/xdoc/onestat.js",
				/*
				 * .js in test is very test specific and a license 
				 * header would confuse and make it unclear what the test 
				 * is about. 
				 */
				"src/test/java",
				/*
				 * See NOTICE.txt
				 */
				"src/main/java/wicket/ajax/wicket-ajax-debug-drag.js"
		};
		
		propertiesIgnore = new String[] {
				/*
				 * .properties in test is very test specific and a license 
				 * header would confuse and make it unclear what the test 
				 * is about. 
				 */
				"src/test/java", 
		};
	}
}
