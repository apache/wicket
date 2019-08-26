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
package org.apache.wicket.markup.head.filter;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.AbstractCspHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;

/**
 * Add a <em>Content Security Policy<em> (CSP) nonce to all relevant JavaScript and CSS header items.
 * <p>
 * Note: please don't forget to wrap with {@link ResourceAggregator} when setting it up with
 * {@link Application#setHeaderResponseDecorator}, otherwise dependencies will not be rendered.
 * 
 * @see AbstractCspHeaderItem
 */
public class CspNonceHeaderResponse extends DecoratingHeaderResponse
{
	private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";

	/**
	 * Has the <em>Content-Security-Policy</em> header been rendered once.
	 */
	private boolean policyRendered = false;

	private String nonce;

	public CspNonceHeaderResponse(IHeaderResponse real, String nonce)
	{
		super(real);

		this.nonce = nonce;
	}

	@Override
	public void render(HeaderItem item)
	{
		while (item instanceof IWrappedHeaderItem)
		{
			item = ((IWrappedHeaderItem)item).getWrapped();
		}

		if (item instanceof AbstractCspHeaderItem)
		{
			if (policyRendered == false)
			{
				// FIXME this one doesn't work with ajax requests, it attempts to re-render the meta with every ajax request
				policyRendered = true;

				String policy = getContentSecurityPolicy(nonce);

				super.render(
						MetaDataHeaderItem.forHttpEquiv(CONTENT_SECURITY_POLICY, policy)
						                  .addTagAttribute("id", "meta-csp")
				);
			}

			((AbstractCspHeaderItem)item).setNonce(nonce);
		}
		
		super.render(item);
	}

	/**
	 * Get the <em>Content-Security-Policy</em> (CSP).
	 * <p>
	 * There is a variety of CSP configurations, this default implementation uses the nonce for scripts and styles
	 * and allows <code>unsafe-eval</code>s (needed for Wicket Ajax).
	 * 
	 * @param nonce
	 *            the nonce
	 * @return content security policy
	 */
	protected String getContentSecurityPolicy(String nonce)
	{
		return String.format("script-src 'unsafe-eval' 'nonce-%1$s'; style-src 'nonce-%1$s';", nonce);
	}
}
