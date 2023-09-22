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
package org.apache.wicket.markup.resolver;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @author Pedro Santos
 */
class AutoLinkResolverTest extends WicketTestCase
{
	private static final Locale DEFAULT_LOCALE = Locale.US;
	private static final Locale EXISTENT_RESOURCE_LOCALE = Locale.CANADA;
	private static final Locale NON_EXISTENT_RESOURCE_LOCALE = Locale.FRANCE;

	@Test
	void shouldAutoLinkLocalizedResources()
	{
		PageWithAutoLinkedLocalResource instance = new PageWithAutoLinkedLocalResource();

		tester.getSession().setLocale(DEFAULT_LOCALE);

		tester.startPage(instance);

		tester.getSession().setLocale(EXISTENT_RESOURCE_LOCALE);

		tester.startPage(instance);

		assertThat(tester.getLastResponseAsString(),
			containsString(EXISTENT_RESOURCE_LOCALE.getCountry()));
	}

	@Test
	void shouldAutoLinkExistentLocalizedResources()
	{
		tester.getSession().setLocale(NON_EXISTENT_RESOURCE_LOCALE);

		tester.startPage(PageWithAutoLinkedLocalResource.class);

		tester.getSession().setLocale(EXISTENT_RESOURCE_LOCALE);

		// works if the page is recreated only
		// TODO: render existent resource's URL if previously not shown by this page instance?
		tester.startPage(PageWithAutoLinkedLocalResource.class);

		assertThat(tester.getLastResponseAsString(),
			containsString(EXISTENT_RESOURCE_LOCALE.getCountry()));
	}
}
