/*
 * $Id: HeaderSectionTest.java 5040 2006-03-20 12:48:09 +0000 (Mon, 20 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-20 12:48:09 +0000 (Mon, 20 Mar
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.parser.filter;

import wicket.WicketRuntimeException;
import wicket.WicketTestCase;
import wicket.markup.html.PackageResource;
import wicket.util.resource.IResourceStream;

/**
 * Simple application that demonstrates the mock http application code (and
 * checks that it is working)
 * 
 * @author Chris Turner
 */
public class HeaderSectionTest extends WicketTestCase
{
	/**
	 * Create the test.
	 * 
	 * @param name
	 *            The test name
	 */
	public HeaderSectionTest(String name)
	{
		super(name);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
		executeTest(HeaderSectionPage_1.class, "HeaderSectionPageExpectedResult_1.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
		executeTest(HeaderSectionPage_2.class, "HeaderSectionPageExpectedResult_2.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_3() throws Exception
	{
		executeTest(HeaderSectionPage_3.class, "HeaderSectionPageExpectedResult_3.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_4() throws Exception
	{
		executeTest(HeaderSectionPage_4.class, "HeaderSectionPageExpectedResult_4.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_5() throws Exception
	{
		executeTest(HeaderSectionPage_5.class, "HeaderSectionPageExpectedResult_5.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_6() throws Exception
	{
		executeTest(HeaderSectionPage_6.class, "HeaderSectionPageExpectedResult_6.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_7() throws Exception
	{
		executeTest(HeaderSectionPage_7.class, "HeaderSectionPageExpectedResult_7.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_8() throws Exception
	{
		executeTest(HeaderSectionPage_8.class, "HeaderSectionPageExpectedResult_8.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_9() throws Exception
	{
		executeTest(HeaderSectionPage_9.class, "HeaderSectionPageExpectedResult_9.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_9a() throws Exception
	{
		executeTest(HeaderSectionPage_9a.class, "HeaderSectionPageExpectedResult_9a.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_10() throws Exception
	{
		executeTest(HeaderSectionPage_10.class, "HeaderSectionPageExpectedResult_10.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_11() throws Exception
	{
		executeTest(HeaderSectionPage_11.class, "HeaderSectionPageExpectedResult_11.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_12() throws Exception
	{
		executeTest(HeaderSectionPage_12.class, "HeaderSectionPageExpectedResult_12.html");
		PackageResource res = (PackageResource)application.getSharedResources().get(
				"wicket.markup.parser.filter.sub.HeaderSectionBorder/cborder.css");
		assertNotNull(res);
		String absPath = res.getAbsolutePath();
		assertNotNull(absPath);
		IResourceStream stream = res.getResourceStream();
		assertNotNull(stream);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_13() throws Exception
	{
		boolean hit = false;
		try
		{
			executeTest(HeaderSectionPage_13.class, "HeaderSectionPageExpectedResult_13.html");
		}
		catch (WicketRuntimeException ex)
		{
			hit = true;
		}
		assertTrue("Expected a MarkupException to be thrown", hit);
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_14() throws Exception
	{
		executeTest(HeaderSectionPage_14.class, "HeaderSectionPageExpectedResult_14.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_15() throws Exception
	{
		executeTest(HeaderSectionPage_15.class, "HeaderSectionPageExpectedResult_15.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_16() throws Exception
	{
		executeTest(HeaderSectionPage_16.class, "HeaderSectionPageExpectedResult_16.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_17() throws Exception
	{
		executeTest(HeaderSectionPage_17.class, "HeaderSectionPageExpectedResult_17.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_18() throws Exception
	{
		executeTest(HeaderSectionPage_18.class, "HeaderSectionPageExpectedResult_18.html");
	}

	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_19() throws Exception
	{
		executeTest(HeaderSectionPage_19.class, "HeaderSectionPageExpectedResult_19.html");
	}
}
