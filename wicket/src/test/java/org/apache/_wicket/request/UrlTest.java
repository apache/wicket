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
package org.apache.wicket.requestng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache._wicket.request.Url;
import org.apache._wicket.request.Url.QueryParameter;

/**
 * @author Matej Knopp
 */
public class UrlTest extends TestCase
{
	private void checkSegments(Url url, String... segments)
	{
		assertEquals(Arrays.asList(segments), url.getSegments());
	}

	private void checkQueryParams(Url url, String... params)
	{
		List<QueryParameter> list = new ArrayList<QueryParameter>();
		for (int i = 0; i < params.length; i += 2)
		{
			QueryParameter p = new QueryParameter(params[i], params[i + 1]);
			list.add(p);
		}

		assertEquals(list, url.getQueryParameters());
	}

	/**
	 * 
	 */
	public void testParse1()
	{
		String s = "foo/bar/baz?a=4&b=5";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "baz");
		checkQueryParams(url, "a", "4", "b", "5");
	}

	/**
	 * 
	 */
	public void testParse2()
	{
		String s = "foo/bar//baz?=4&6";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "bar", "", "baz");
		checkQueryParams(url, "", "4", "6", "");
	}

	/**
	 * 
	 */
	public void testParse3()
	{
		String s = "//foo/bar/";
		Url url = Url.parse(s);
		checkSegments(url, "", "", "foo", "bar", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse4()
	{
		String s = "/foo/bar//";
		Url url = Url.parse(s);
		checkSegments(url, "", "foo", "bar", "", "");
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse5()
	{
		String s = "foo/b%3Dr/b%26z/x%3F?a=b&x%3F%264=y%3Dz";
		Url url = Url.parse(s);
		checkSegments(url, "foo", "b=r", "b&z", "x?");
		checkQueryParams(url, "a", "b", "x?&4", "y=z");
	}

	/**
	 * 
	 */
	public void testParse6()
	{
		String s = "";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url);
	}

	/**
	 * 
	 */
	public void testParse7()
	{
		String s = "?a=b";
		Url url = Url.parse(s);
		checkSegments(url);
		checkQueryParams(url, "a", "b");
	}
	
	/**
	 * 
	 */
	public void testParse8()
	{
		String s = "/";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url);
	}
	
	/**
	 * 
	 */
	public void testParse9()
	{
		String s = "/?a=b";
		Url url = Url.parse(s);
		checkSegments(url, "", "");
		checkQueryParams(url, "a", "b");
	}

	/**
	 * 
	 */
	public void testRender1()
	{
		Url url = new Url();
		url.getSegments().add("foo");
		url.getSegments().add("b=r");
		url.getSegments().add("b&z");
		url.getSegments().add("x?");
		url.setQueryParameter("a", "b");
		url.setQueryParameter("x?&4", "y=z");

		assertEquals("foo/b%3Dr/b%26z/x%3F?a=b&x%3F%264=y%3Dz", url.toString());
	}

}
