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

import junit.framework.TestCase;

/**
 * Tests {@link JavascriptStripper}
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class JavascriptStripperTest extends TestCase
{
	public void testUNIXWICKET501()
	{
		String s = JavascriptStripper
				.stripCommentsAndWhitespace("    // Handle the common XPath // expression\n    if ( !t.indexOf(\"//\") ) {");
		assertEquals("\n\nif ( !t.indexOf(\"//\") ) {", s);
	}

	public void testDOSWICKET501()
	{
		String s = JavascriptStripper
				.stripCommentsAndWhitespace("    // Handle the common XPath // expression\r\n    if ( !t.indexOf(\"//\") ) {");
		assertEquals("\n\nif ( !t.indexOf(\"//\") ) {", s);
	}

	public void testMACWICKET501()
	{
		String s = JavascriptStripper
				.stripCommentsAndWhitespace("    // Handle the common XPath // expression\r    if ( !t.indexOf(\"//\") ) {");
		assertEquals("\n\nif ( !t.indexOf(\"//\") ) {", s);
	}

	public void testRegexp()
	{
		String s = JavascriptStripper
				.stripCommentsAndWhitespace("    t = jQuery.trim(t).replace( /^\\/\\//i, \"\" );");
		assertEquals("\nt = jQuery.trim(t).replace( /^\\/\\//i, \"\" );", s);
	}
	
	public void testRegexpWithString()
	{
		String s = JavascriptStripper
				.stripCommentsAndWhitespace("foo.replace(/\"//*strip me*/, \"\"); // strip me\rdoFoo();");
		assertEquals("foo.replace(/\"/, \"\"); doFoo();", s);
	}
}
