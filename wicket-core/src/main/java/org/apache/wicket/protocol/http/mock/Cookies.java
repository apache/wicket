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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cookies
{
	private final static Logger log = LoggerFactory.getLogger(CookieUtils.class);

	/**
	 * make a copy
	 * 
	 * @param source
	 * @return exact copy
	 */
	public static Cookie copyOf(Cookie source)
	{
		return (Cookie)source.clone();
	}

	/**
	 * make a copy
	 * 
	 * @param source
	 * @return exact copy
	 */
	public static List<Cookie> copyOf(List<Cookie> source)
	{
		List<Cookie> cloned = Generics.newArrayList();
		for (Cookie c : source)
		{
			cloned.add(copyOf(c));
		}
		return cloned;
	}

	/**
	 * make a copy
	 * 
	 * @param source
	 * @return exact copy
	 */
	public static Cookie[] copyOf(Cookie[] source)
	{
		Cookie[] cloned = new Cookie[source.length];
		for (int i = 0; i < source.length; i++)
		{
			cloned[i] = copyOf(source[i]);
		}
		return cloned;
	}

	private static List<Cookie> notExpiredCookies(List<Cookie> cookies)
	{
		List<Cookie> stillLeft = Generics.newArrayList();
		for (Cookie cookie : cookies)
		{
			// maxAge == -1 -> means session cookie
			// maxAge == 0 -> delete the cookie
			// maxAge > 0 -> the cookie will expire after this age
			if (cookie.getMaxAge() != 0)
			{
				stillLeft.add(cookie);
			}
		}
		return stillLeft;
	}

	/**
	 * create a cookie source which filters expired cookies
	 * 
	 * @param source
	 * @return filtered cookie source
	 */
	public static ICookieSource notExpiredCookies(final ICookieSource source)
	{
		return new ICookieSource()
		{
			@Override
			public List<Cookie> getCookiesAsList()
			{
				return source != null ? notExpiredCookies(source.getCookiesAsList())
					: Generics.<Cookie> newArrayList();
			}
		};
	}

	/**
	 * creates a cookie source which joins the result of other cookie sources
	 * 
	 * @param source
	 * @return joined sources
	 */
	public static ICookieSource allOf(final ICookieSource... source)
	{
		return new ICookieSource()
		{
			@Override
			public List<Cookie> getCookiesAsList()
			{
				ArrayList<Cookie> all = Generics.newArrayList();
				for (ICookieSource s : source)
				{
					if (s != null)
						all.addAll(s.getCookiesAsList());
				}
				return all;
			}
		};
	}

	/**
	 * creates a cookie source which filters a source by name and take the last value in resulting
	 * list
	 * 
	 * @param source
	 * @return filtered cookie source
	 */
	public static ICookieSource lastValue(final ICookieSource source)
	{
		return new ICookieSource()
		{
			@Override
			public List<Cookie> getCookiesAsList()
			{
				HashMap<String, Cookie> map = new LinkedHashMap<String, Cookie>();
				for (Cookie cookie : source.getCookiesAsList())
				{
					map.put(cookie.getName(), cookie);
				}
				ArrayList<Cookie> cookies = Generics.newArrayList();
				cookies.addAll(map.values());
				return cookies;
			}
		};
	}


	/**
	 * set result from cookie source to cookie destination
	 * 
	 * @param source
	 * @param destination
	 */
	public static void set(ICookieSource source, ICookieDestination destination)
	{
		destination.addCookies(source.getCookiesAsList());
	}
}
