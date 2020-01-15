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
package org.apache.wicket.examples.csp;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.CspNonceHeaderResponse;

public class CspApplication extends WicketExampleApplication
{
	private static final int NONCE_LENGTH = 24;
	
	public static MetaDataKey<String> NONCE_KEY = new MetaDataKey<String>()
	{
	};

	@Override
	public Class<? extends Page> getHomePage()
	{
		return NonceDemoPage.class;
	}

	@Override
	protected void init()
	{
		super.init();

		getHeaderResponseDecorators().add(response -> new CspNonceHeaderResponse(response, getNonce()));
		
		mountPage("noncedemo", NonceDemoPage.class);
	}

	protected static String generateNonce()
	{
		byte[] randomBytes = new byte[NONCE_LENGTH];
		ThreadLocalRandom.current().nextBytes(randomBytes);
		return Base64.getUrlEncoder().encodeToString(randomBytes);
	}

	public static String getNonce()
	{
		Session session = Session.get();
		session.bind();
		String nonce = session.getMetaData(NONCE_KEY);
		if (nonce == null)
		{
			nonce = generateNonce();
			session.setMetaData(NONCE_KEY, nonce);
		}
		return nonce;
	}
}
