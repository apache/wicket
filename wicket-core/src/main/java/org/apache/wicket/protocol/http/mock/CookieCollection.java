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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.wicket.protocol.http.mock.Cookies.Key;

/**
 * cookie collection utility
 * @author mosmann
 */
public class CookieCollection
{
	Map<Cookies.Key, Cookie> cookies = new LinkedHashMap<Cookies.Key, Cookie>();
	Map<Cookies.Key, Cookie> expiredCookies = new LinkedHashMap<Cookies.Key, Cookie>();

	/**
	 * add cookie to collection
	 *   if cookie is expired, it will be moved to expired cookie set
	 *   overwrite existing cookie with new value 
	 * @param cookie a cookie
	 */
	public void add(Cookie cookie)
	{
		Key key = Cookies.keyOf(cookie);
		Cookie copyOfCookie = Cookies.copyOf(cookie);
		if (isExpired(cookie))
		{
			expiredCookies.put(key, copyOfCookie);
			cookies.remove(key);
		}
		else
		{
			cookies.put(key, copyOfCookie);
		}
	}

	/**
	 * calls add on each cookie
	 * @param cookies array of cookies
	 */
	public void addAll(Cookie[] cookies)
	{
		if (cookies != null)
			addAll(Arrays.asList(cookies));
	}

	/**
	 * calls add on each cookie
	 * @param cookies list of cookies
	 */
	public void addAll(List<Cookie> cookies)
	{
		for (Cookie cookie : cookies)
		{
			add(cookie);
		}
	}

	/**
	 * list of non expired cookies
	 * @return as list
	 */
	public List<Cookie> asList()
	{
		ArrayList<Cookie> ret = new ArrayList<Cookie>();
		ret.addAll(cookies.values());
		return ret;
	}

	/**
	 * list of expired cookies
	 * @return as list
	 */
	public List<Cookie> expiredAsList()
	{
		ArrayList<Cookie> ret = new ArrayList<Cookie>();
		ret.addAll(expiredCookies.values());
		return ret;
	}

	/**
	 * list of all cookies, expired or not
	 * @return as list
	 */
	public List<Cookie> allAsList()
	{
		ArrayList<Cookie> ret = new ArrayList<Cookie>();
		ret.addAll(cookies.values());
		ret.addAll(expiredCookies.values());
		return ret;
	}

	/**
	 * detect if this cookie is expired
	 * 
	 * @param cookie
	 * @return true, if expired
	 */
	public static boolean isExpired(Cookie cookie)
	{
		return cookie.getMaxAge() == 0;
	}


}
