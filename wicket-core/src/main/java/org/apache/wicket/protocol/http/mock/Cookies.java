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
package org.apache.wicket.protocol.http.mock;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.util.lang.Generics;

public class Cookies
{
	public static Cookie copyOf(Cookie source)
	{
		return (Cookie)source.clone();
	}

	public static List<Cookie> copyOf(List<Cookie> source)
	{
		List<Cookie> cloned = Generics.newArrayList();
		for (Cookie c : source)
		{
			cloned.add(copyOf(c));
		}
		return cloned;
	}

	public static Cookie[] copyOf(Cookie[] source)
	{
		Cookie[] cloned = new Cookie[source.length];
		for (int i = 0; i < source.length; i++)
		{
			cloned[i] = copyOf(source[i]);
		}
		return cloned;
	}
}
