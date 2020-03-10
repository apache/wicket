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
package org.apache.wicket.csp;

import org.apache.wicket.markup.head.AbstractCspHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Add a <em>Content Security Policy<em> (CSP) nonce to all {@link AbstractCspHeaderItem}s when that
 * is required by the configuration of the CSP.
 */
public class CSPNonceHeaderResponseDecorator extends DecoratingHeaderResponse
{
	private final ContentSecurityPolicySettings settings;

	public CSPNonceHeaderResponseDecorator(IHeaderResponse real, ContentSecurityPolicySettings settings)
	{
		super(real);

		this.settings = settings;
	}

	@Override
	public void render(HeaderItem item)
	{
		if (settings.isNonceEnabled())
		{
			HeaderItem checkitem = item;
			while (checkitem instanceof IWrappedHeaderItem)
			{
				checkitem = ((IWrappedHeaderItem) checkitem).getWrapped();
			}

			if (checkitem instanceof AbstractCspHeaderItem)
			{
				((AbstractCspHeaderItem) checkitem).setNonce(settings.getNonce(RequestCycle.get(),
					RequestCycle.get().getActiveRequestHandler()));
			}
		}

		super.render(item);
	}
}
