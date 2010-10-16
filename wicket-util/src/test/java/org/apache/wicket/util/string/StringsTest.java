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
 * 
 */
public class StringsTest extends TestCase
{
	public void testStripJSessionId()
	{
		String url = "http://localhost/abc";
		assertEquals(url, Strings.stripJSessionId(url));
		assertEquals(url + "/", Strings.stripJSessionId(url + "/"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + "?param"));
		assertEquals(url + "?param=a;b", Strings.stripJSessionId(url + "?param=a;b"));
		assertEquals(url + "/?param", Strings.stripJSessionId(url + "/?param"));
		assertEquals(url, Strings.stripJSessionId(url + ";jsessionid=12345"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + ";jsessionid=12345?param"));
		assertEquals(url + "?param=a;b",
			Strings.stripJSessionId(url + ";jsessionid=12345?param=a;b"));
	}
}
