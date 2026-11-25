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
package org.apache.wicket.examples.upload;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.regex.Pattern;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import jakarta.servlet.http.Cookie;

/**
 * Knows a visitor of the examples by a random token in a cookie that lasts a week, so that the
 * resumable uploads of the visitor outlive the session. An application whose users sign in uses
 * the signed-in user instead. The token is a secret like the id of a session: long, random, set by
 * the server and kept from scripts.
 */
final class UploadVisitor
{
	private static final String COOKIE = "wicket-examples-upload-visitor";

	private static final Pattern TOKEN = Pattern.compile("[0-9a-f]{32}");

	private static final SecureRandom RANDOM = new SecureRandom();

	/** The token a request gave a visitor without one, for the rest of the request. */
	private static final MetaDataKey<String> NEW_TOKEN = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private UploadVisitor()
	{
	}

	/**
	 * @return the owner of the uploads of the visitor of the current request, a new one, and its
	 *         cookie, for a visitor without
	 */
	static String owner()
	{
		RequestCycle cycle = RequestCycle.get();
		if (cycle == null || !(cycle.getRequest() instanceof WebRequest request))
		{
			return null;
		}
		Cookie cookie = request.getCookie(COOKIE);
		String token = cookie != null && TOKEN.matcher(cookie.getValue()).matches()
			? cookie.getValue() : cycle.getMetaData(NEW_TOKEN);
		// the response of the request, not that of a part being rendered
		if (token == null && cycle.getOriginalResponse() instanceof WebResponse response)
		{
			byte[] random = new byte[16];
			RANDOM.nextBytes(random);
			token = HexFormat.of().formatHex(random);
			Cookie set = new Cookie(COOKIE, token);
			set.setMaxAge((int)Duration.ofDays(7).toSeconds());
			set.setHttpOnly(true);
			set.setSecure(request.getUrl().getProtocol() != null &&
				request.getUrl().getProtocol().equals("https"));
			String context = request.getContextPath();
			set.setPath(context == null || context.isEmpty() ? "/" : context);
			response.addCookie(set);
			cycle.setMetaData(NEW_TOKEN, token);
		}
		return token != null ? "visitor:" + token : null;
	}
}
