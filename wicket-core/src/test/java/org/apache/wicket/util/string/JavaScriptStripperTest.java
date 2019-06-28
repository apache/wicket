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
package org.apache.wicket.util.string;

import org.apache.wicket.core.util.string.JavaScriptStripper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JavaScriptStripper}
 *
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
class JavaScriptStripperTest
{
	/**	 */
	@Test
	void unixWICKET501()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("    // Handle the common XPath // expression\n    if ( !t.indexOf(\"//\") ) {");
		assertEquals(" \n if ( !t.indexOf(\"//\") ) {", s);
	}

	/**	 */
	@Test
	void dosWICKET501()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("    // Handle the common XPath // expression\r\n    if ( !t.indexOf(\"//\") ) {");
		assertEquals(" \r\nif ( !t.indexOf(\"//\") ) {", s);
	}

	/**	 */
	@Test
	void macWICKET501()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("    // Handle the common XPath // expression\r    if ( !t.indexOf(\"//\") ) {");
		assertEquals(" \r if ( !t.indexOf(\"//\") ) {", s);
	}

	/**	 */
	@Test
	void regexp()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("    t = jQuery.trim(t).replace( /^\\/\\//i, \"\" );");
		assertEquals(" t = jQuery.trim(t).replace( /^\\/\\//i, \"\" );", s);
	}

	/**	 */
	@Test
	void regexp2()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("foo.replace(/\"//*strip me*/, \"\"); // strip me\rdoFoo();");
		assertEquals("foo.replace(/\"/, \"\"); \rdoFoo();", s);
	}

	/**	 */
	@Test
	void regexp3()
	{
		String s = new JavaScriptStripper().stripCommentsAndWhitespace("parseFloat( elem.filter.match(/alpha\\(opacity=(.*)\\)/)[1] ) / 100 : 1;\r//foo");
		assertEquals("parseFloat( elem.filter.match(/alpha\\(opacity=(.*)\\)/)[1] ) / 100 : 1;\r",
			s);
	}

	/**	 */
	@Test
	void regexp4()
	{
		String before = " attr: /**/ //xyz\n /\\[((?:[\\w-]*:)?[\\w-]+)\\s*(?:([!^$*~|]?=)\\s*((['\"])([^\\4]*?)\\4|([^'\"][^\\]]*?)))?\\]/    after     regex";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = " attr:  \n /\\[((?:[\\w-]*:)?[\\w-]+)\\s*(?:([!^$*~|]?=)\\s*((['\"])([^\\4]*?)\\4|([^'\"][^\\]]*?)))?\\]/ after regex";
		assertEquals(expected, after);
		System.out.println(after);
	}

	/**	 */
	@Test
	void WICKET1806()
	{
		String before = "a = [ /^(\\[) *@?([\\w-]+) *([!*$^~=]*) *('?\"?)(.*?)\\4 *\\]/ ];    b()";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = "a = [ /^(\\[) *@?([\\w-]+) *([!*$^~=]*) *('?\"?)(.*?)\\4 *\\]/ ]; b()";

		assertEquals(expected, after);
	}

	/**	 */
	@Test
	void WICKET2060_1()
	{
		String before = "   a   b   c";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = " a b c";
		assertEquals(expected, after);
	}

	/**	 */
	@Test
	void WICKET2060_2()
	{
		String before = "   a \n  b   c\n\n";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = " a\nb c\n";
		assertEquals(expected, after);
	}

	/**	 */
	@Test
	void WICKET2060_3()
	{
		String before = "return  this.__unbind__(type, fn);";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = "return this.__unbind__(type, fn);";
		assertEquals(expected, after);
	}

	/**     */
	@Test
	void WICKET4760()
	{
		String before = "x++ //\nx++";
		String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);
		String expected = "x++ \nx++";
		assertEquals(expected, after);
	}

	/**     */
	// @formatter:off
	private static String TESTSTRING2 =
         "   var test = function () {\n" +
         "   var c = \"!=\";\n" +
         "    /* from jquery 1.5.1 */\n" +
         "    if ( !l.match.PSEUDO.test(c) && !/!=/.test(c)) {\n" +
         "       alert(\"/something bad will happen */* \");\n" +
         "   }\n" +
         "\n" +
         "     var importantFunction = function () {alert(\"really important function \")}\n" +
         "   /*\n" +
         "     This code will be stripped\n" +
         "   */\n" +
         "\n" +
         "}" ;
	// @formatter:on

	/**	 */
	@Test
	void regExThatStartsWithExclamationMark()
	{
		String result = new JavaScriptStripper().stripCommentsAndWhitespace(TESTSTRING2);
		assertFalse(result.contains("This code will be stripped"));
		assertTrue(result.contains("something bad will happen"));
		assertTrue(result.contains("really important function"));

		System.out.println(result);
	}

	/**	*/
	@Test
	void templateLiteralWithTwoForwardSlashes() {
		final String before = "const url = `${protocol}//${hostname}:${port}`;";
		final String after = new JavaScriptStripper().stripCommentsAndWhitespace(before);

		assertEquals(before, after);
	}


}
