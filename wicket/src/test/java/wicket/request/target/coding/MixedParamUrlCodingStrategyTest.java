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
package wicket.request.target.coding;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import wicket.markup.html.WebPage;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.tester.WicketTester;
import wicket.util.value.ValueMap;

/**
 * Tests for {@link MixedParamUrlCodingStrategy}.
 * 
 * @author erik.van.oosten
 */
public class MixedParamUrlCodingStrategyTest extends TestCase
{
	/**
	 * Test class.
	 * 
	 * @author erik.van.oosten
	 */
	public static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;
		// EMPTY
	}

	private WicketTester tester;

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap1()
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("a", "1");
		parameters.put("d", "4");
		parameters.put("e", "5");

		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		String urlStr = url.toString();
		assertTrue("/1?d=4&e=5".equals(urlStr) || "/1?e=5&d=4".equals(urlStr));
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap2()
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("a", "1");
		parameters.put("b", "2");
		parameters.put("c", "3");
		parameters.put("d", "4");
		parameters.put("e", "5");

		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		String urlStr = url.toString();
		assertTrue("/1/2/3?d=4&e=5".equals(urlStr) || "/1/2/3?e=5&d=4".equals(urlStr));
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap3()
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("a", "1");
		parameters.put("b", "2");
		parameters.put("c", "3");
		parameters.put("d", "4");
		parameters.put("e", "5");

		String[] parameterNames = new String[] {};
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		String urlStr = url.toString();
		assertEquals(20, urlStr.length());
		assertTrue(urlStr.indexOf("a=1") != -1);
		assertTrue(urlStr.indexOf("b=2") != -1);
		assertTrue(urlStr.indexOf("c=3") != -1);
		assertTrue(urlStr.indexOf("d=4") != -1);
		assertTrue(urlStr.indexOf("e=5") != -1);
		assertTrue(urlStr.matches("^\\?([abcde]=[12345]&){4}([abcde]=[12345])$"));
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap4()
	{
		Map<String, String> parameters = new HashMap<String, String>();

		String[] parameterNames = new String[] { "a", "b" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		assertEquals("", url.toString());
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap5()
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("a", "1");
		parameters.put("b", "2");
		parameters.put("c", "3");

		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		assertEquals("/1/2/3", url.toString());
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#appendParameters(wicket.util.string.AppendingStringBuffer, java.util.Map)}.
	 */
	public void testAppendParametersAppendingStringBufferMap6()
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("a", "1");
		parameters.put("c", "3");
		parameters.put("d", "4");
		parameters.put("e", "5");

		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		AppendingStringBuffer url = new AppendingStringBuffer(40);
		npucs.appendParameters(url, parameters);
		String urlStr = url.toString();
		assertTrue("/1//3?d=4&e=5".equals(urlStr) || "/1//3?e=5&d=4".equals(urlStr));
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap1()
	{
		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put("d", "4");
		urlMap.put("e", "5");

		ValueMap parameterMap = npucs.decodeParameters("/1", urlMap);
		assertEquals(3, parameterMap.size());
		assertContains(parameterMap, "a", "1");
		assertContains(parameterMap, "d", "4");
		assertContains(parameterMap, "e", "5");
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap2()
	{
		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put("d", "4");
		urlMap.put("e", "5");

		ValueMap parameterMap = npucs.decodeParameters("/1/2/3", urlMap);
		assertEquals(5, parameterMap.size());
		assertContains(parameterMap, "a", "1");
		assertContains(parameterMap, "b", "2");
		assertContains(parameterMap, "c", "3");
		assertContains(parameterMap, "d", "4");
		assertContains(parameterMap, "e", "5");
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap3()
	{
		String[] parameterNames = new String[] {};
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put("a", "1");
		urlMap.put("b", "2");
		urlMap.put("c", "3");
		urlMap.put("d", "4");
		urlMap.put("e", "5");

		ValueMap parameterMap = npucs.decodeParameters("", urlMap);
		assertEquals(5, parameterMap.size());
		assertContains(parameterMap, "a", "1");
		assertContains(parameterMap, "b", "2");
		assertContains(parameterMap, "c", "3");
		assertContains(parameterMap, "d", "4");
		assertContains(parameterMap, "e", "5");
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap4()
	{
		String[] parameterNames = new String[] { "a", "b" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		Map urlMap = new HashMap();

		ValueMap parameterMap = npucs.decodeParameters("", urlMap);
		assertEquals(0, parameterMap.size());
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap5()
	{
		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		Map urlMap = new HashMap();

		ValueMap parameterMap = npucs.decodeParameters("/1/2/3", urlMap);
		assertEquals(3, parameterMap.size());
		assertContains(parameterMap, "a", "1");
		assertContains(parameterMap, "b", "2");
		assertContains(parameterMap, "c", "3");
	}

	/**
	 * Test method for
	 * {@link MixedParamUrlCodingStrategy#decodeParameters(java.lang.String, java.util.Map)}.
	 */
	public void testDecodeParametersStringMap6()
	{
		String[] parameterNames = new String[] { "a", "b", "c" };
		MixedParamUrlCodingStrategy npucs = new MixedParamUrlCodingStrategy("mnt", TestPage.class,
				parameterNames);

		// Note nasty, but ignored c parameter
		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put("c", "XXXXXXX");
		urlMap.put("d", "4");
		urlMap.put("e", "5");

		ValueMap parameterMap = npucs.decodeParameters("/1//3", urlMap);
		assertEquals(5, parameterMap.size());
		assertContains(parameterMap, "a", "1");
		// Note: missing b is translated to empty string.
		assertContains(parameterMap, "b", "");
		assertContains(parameterMap, "c", "3");
		assertContains(parameterMap, "d", "4");
		assertContains(parameterMap, "e", "5");
	}

	/** {@inheritDoc} */
	protected void setUp() throws Exception
	{
		tester = new WicketTester();
		tester.startPage(TestPage.class);
		tester.getApplication().getRequestCycleSettings().setResponseRequestEncoding("UTF-8");
	}

	/**
	 * @param parameterMap
	 *            a map
	 * @param key
	 *            expected key
	 * @param value
	 *            expected value
	 */
	private void assertContains(Map parameterMap, String key, String value)
	{
		assertEquals(value, parameterMap.get(key));
	}
}
