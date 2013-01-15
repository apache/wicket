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
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cookies
{
	private final static Logger log = LoggerFactory.getLogger(CookieUtils.class);

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


	public static void set(ICookieSource source, ICookieDestination destination)
	{
		destination.addCookies(source.getCookiesAsList());
	}

	public static void logCookies(String message, ICookieSource source)
	{
		// if (log.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder();
			sb.append(message).append(" -> ");
			if (source != null)
			{
				for (Cookie cookie : source.getCookiesAsList())
				{
					sb.append(cookieToDebugString(cookie));
					sb.append(",");
				}
			}
			else
			{
				sb.append("--not-set--");
			}
			log.error(sb.toString());
		}
	}


	/**
	 * Gets debug info as a string for the given cookie.
	 * 
	 * @param cookie
	 *            the cookie to debug.
	 * @return a string that represents the internals of the cookie.
	 */
	private static String cookieToDebugString(final Cookie cookie)
	{
		return "[Cookie " + " name = " + cookie.getName() + ", value = " + cookie.getValue() +
			", domain = " + cookie.getDomain() + ", path = " + cookie.getPath() + ", maxAge = " +
			Time.millis(cookie.getMaxAge()).toDateString() + "(" + cookie.getMaxAge() + ")" + "]";
	}


}
