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
package org.apache.wicket.protocol.https;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.IPageRequestHandler;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link HttpsRequestChecker}
 */
public class HttpsRequestCheckerTest extends WicketTestCase
{

	/**
	 * Asserts that
	 * {@link HttpsRequestChecker#checkSecureIncoming(org.apache.wicket.request.IRequestHandler, HttpsConfig)}
	 * returns {@link SwitchProtocolRequestHandler} for pages annotated with {@link RequireHttps} or
	 * returns the original {@link IRequestHandler} for pages that are not annotated with
	 * {@link RequireHttps}
	 */
	@Test
	public void checkSecureIncoming()
	{
		HttpsRequestChecker checker = new HttpsRequestChecker();

		IPageRequestHandler httpsPageRequestHandler = Mockito.mock(IPageRequestHandler.class);
		Mockito.doReturn(HttpsPage.class).when(httpsPageRequestHandler).getPageClass();

		HttpsConfig httpsConfig = new HttpsConfig();

		IRequestHandler httpsPageSecureIncoming = checker.checkSecureIncoming(
			httpsPageRequestHandler, httpsConfig);
		assertTrue(httpsPageSecureIncoming instanceof SwitchProtocolRequestHandler);

		IPageRequestHandler httpPageRequestHandler = Mockito.mock(IPageRequestHandler.class);
		Mockito.doReturn(HttpPage.class).when(httpsPageRequestHandler).getPageClass();

		IRequestHandler httpPageSecureIncoming = checker.checkSecureIncoming(
			httpPageRequestHandler, httpsConfig);
		assertSame(httpPageRequestHandler, httpPageSecureIncoming);
	}
}
