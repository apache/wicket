/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import junit.framework.TestCase;

/**
 * Unit test for the PageParameters, introduced for bug
 * [ 1213889 ] PageParameter keyValuePair disallows negatives.
 * 
 * @author Martijn Dashorst
 */
public class PageParametersTest extends TestCase
{
	/**
	 * Parsing of negative numbers on the right side of the assignment
	 * didn't work, as the minus character was not part of the word
	 * pattern.
	 */
	public void testNegativeNumberParameter()
	{
		PageParameters parameters = new PageParameters("a=-1");
		assertEquals("-1", parameters.get("a"));
	}

	/**
	 * 
	 */
	public void test_1()
	{
		PageParameters parameters = new PageParameters("0=test");
		assertEquals("test", parameters.get("0"));
	}

	/**
	 * 
	 */
	public void test_2()
	{
		PageParameters parameters = new PageParameters("test");
		assertNull(parameters.get("test"));
	}

	/**
	 * 
	 */
	public void test_3()
	{
		PageParameters parameters = new PageParameters("test=");
		assertEquals("", parameters.get("test"));
	}

	/**
	 * 
	 */
	public void test_4()
	{
		try
		{
			PageParameters parameters = new PageParameters("=test");
			fail("Expected an exception: invalid URL parameter");
		}
		catch(IllegalArgumentException ex)
		{
			// ok; expected
		}
	}
}
