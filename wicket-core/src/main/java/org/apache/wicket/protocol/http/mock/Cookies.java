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

import javax.servlet.http.Cookie;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;

/**
 * A helper class for dealing with cookies
 */
public final class Cookies
{
	/**
	 * Constructor.
	 */
	private Cookies()
	{}

	/**
	 * Make a copy of the passed cookie.
	 *
	 * @param cookie
	 *          The cookie to copy
	 * @return A copy of the passed cookie. May be {@code null} if the argument is {@code null}.
	 */
	public static Cookie copyOf(Cookie cookie)
	{
		return cookie != null ? (Cookie) cookie.clone() : null;
	}

	/**
	 * Checks whether two cookies are equal.
	 * See http://www.ietf.org/rfc/rfc2109.txt, p.4.3.3
	 *
	 * @param c1
	 *      the first cookie
	 * @param c2
	 *      the second cookie
	 * @return {@code true} only if the cookies have the same name, path and domain
	 */
	public static boolean isEqual(Cookie c1, Cookie c2)
	{
		Args.notNull(c1, "c1");
		Args.notNull(c2, "c2");

		return c1.getName().equals(c2.getName()) &&
				Objects.isEqual(c1.getPath(), c2.getPath()) &&
				Objects.isEqual(c1.getDomain(), c2.getDomain());
	}
}
