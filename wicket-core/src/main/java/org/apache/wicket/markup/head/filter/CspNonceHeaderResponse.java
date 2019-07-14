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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IWrappedHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;

/**
 * Add CSP nonce to all relevant JavaScript and CSS header items
 * <p>
 * Note: please don't forget to wrap with {@link ResourceAggregator}
 * when setting it up with {@link Application#setHeaderResponseDecorator},
 * otherwise dependencies will not be rendered
 */
public abstract class CspNonceHeaderResponse extends DecoratingHeaderResponse
{
	public CspNonceHeaderResponse(IHeaderResponse real)
	{
		super(real);
	}

	@Override
	public void render(HeaderItem item)
	{
		while (item instanceof IWrappedHeaderItem)
		{
			item = ((IWrappedHeaderItem)item).getWrapped();
		}
		final String nonce = getNonce();
		if (item instanceof JavaScriptHeaderItem)
		{
			((JavaScriptHeaderItem)item).setNonce(nonce);
		} else if (item instanceof OnDomReadyHeaderItem)
		{
			((OnDomReadyHeaderItem)item).setNonce(nonce);
		} else if (item instanceof OnLoadHeaderItem)
		{
			((OnLoadHeaderItem)item).setNonce(nonce);
		} else if (item instanceof CssHeaderItem)
		{
			((CssHeaderItem)item).setNonce(nonce);
		}
		super.render(item);
	}

	protected abstract String getNonce();
}
