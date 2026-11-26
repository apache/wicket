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
package org.apache.wicket.dbsc;

import org.apache.wicket.request.cycle.RequestCycle;

/**
 * What a request on a bound session that arrived without a valid bound cookie carried, for
 * {@link DeviceBoundSessions#onMissingCredential(RequestCycle, MissingCredential)} to tell its
 * causes apart.
 * <p>
 * The values come from the request and are only as trustworthy as its sender: a request with a
 * copied session cookie can claim any of them. Use them to choose how to answer, not to accept
 * the request anyway.
 *
 * @since 12.0.0
 */
public final class MissingCredential
{
	private final boolean cookiePresent;

	private final boolean crossSite;

	private final String skippedRefresh;

	MissingCredential(boolean cookiePresent, boolean crossSite, String skippedRefresh)
	{
		this.cookiePresent = cookiePresent;
		this.crossSite = crossSite;
		this.skippedRefresh = skippedRefresh;
	}

	/**
	 * @return whether the request carried a bound cookie at all, one that has expired or was never
	 *         issued for the session
	 */
	public boolean isCookiePresent()
	{
		return cookiePresent;
	}

	/**
	 * @return whether the browser says another site started the request, in
	 *         {@code Sec-Fetch-Site: cross-site}. The browser does not refresh the bound cookie
	 *         for such a request unless the site is one of
	 *         {@link DeviceBoundSessions#getAllowedRefreshInitiators()}.
	 */
	public boolean isCrossSite()
	{
		return crossSite;
	}

	/**
	 * @return why the browser sent the request without refreshing the bound cookie first, as it
	 *         says in {@code Secure-Session-Skipped} for this session: {@code unreachable} or
	 *         {@code server_error} for a refresh endpoint that failed, {@code quota_exceeded} for
	 *         a browser that ran out of signing operations, or {@code null} if it says nothing
	 */
	public String getSkippedRefresh()
	{
		return skippedRefresh;
	}

	@Override
	public String toString()
	{
		return "MissingCredential [cookiePresent=" + cookiePresent + ", crossSite=" + crossSite +
			", skippedRefresh=" + skippedRefresh + "]";
	}
}
