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

import java.io.Serializable;

import javax.servlet.http.Cookie;

import org.apache.wicket.util.lang.Args;

/**
 * A helper class for dealing with cookies
 */
public final class Cookies
{
	/**
	 * Constructor.
	 */
	private Cookies()
	{
	}

	/**
	 * Make a copy of the passed cookie.
	 * 
	 * @param cookie
	 *            The cookie to copy
	 * @return A copy of the passed cookie. May be {@code null} if the argument is {@code null}.
	 */
	public static Cookie copyOf(Cookie cookie)
	{
		return cookie != null ? (Cookie)cookie.clone() : null;
	}

	/**
	 * creates a key based on the property for cookie equality
	 * 
	 * @param cookie
	 *            cookie
	 * @return key
	 */
	public static Key keyOf(Cookie cookie)
	{
		return new Key(cookie);
	}

	/**
	 * Checks whether two cookies are equal. 
	 * See http://www.ietf.org/rfc/rfc2109.txt, p.4.3.3
	 * 
	 * @param c1
	 *            the first cookie
	 * @param c2
	 *            the second cookie
	 * @return {@code true} only if the cookies have the same name, path and domain
	 */
	public static boolean isEqual(Cookie c1, Cookie c2)
	{
		Args.notNull(c1, "c1");
		Args.notNull(c2, "c2");

		return new Key(c1).equals(new Key(c2));
	}

	/**
	 * detect if this cookie is expired
	 * 
	 * @param cookie
	 * @return
	 */
	public static boolean isExpired(Cookie cookie)
	{
		return cookie.getMaxAge() == 0;
	}

	public static class Key implements Serializable
	{

		private final String name;
		private final String path;
		private final String domain;

		protected Key(Cookie cookie)
		{
			name = cookie.getName();
			path = cookie.getPath();
			domain = cookie.getDomain();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((domain == null) ? 0 : domain.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key)obj;
			if (domain == null)
			{
				if (other.domain != null)
					return false;
			}
			else if (!domain.equals(other.domain))
				return false;
			if (name == null)
			{
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			if (path == null)
			{
				if (other.path != null)
					return false;
			}
			else if (!path.equals(other.path))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return name + ";" + domain + "/" + path;
		}

	}

}
