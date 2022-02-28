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
package org.apache.wicket.protocol.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Locale;
import java.util.stream.Stream;

import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.mock.MockWebRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Timo Rantalaiho
 */
class WebSessionTest
{
	/**
	 * testReadsLocaleFromRequestOnConstruction()
	 */
	@Test
	void readsLocaleFromRequestOnConstruction()
	{
		final Locale locale = Locale.TRADITIONAL_CHINESE;
		MockWebRequest request = new MockWebRequest(Url.parse("/"))
		{
			@Override
			public Locale getLocale()
			{
				return locale;
			}
		};

		WebSession session = new WebSession(request);
		assertEquals(locale, session.getLocale());
	}

	@Test
	public void changeSessionId() throws Exception
	{
		WicketTester tester = new WicketTester(new MockApplication());
		MockHttpSession httpSession = (MockHttpSession)tester.getRequest().getSession();
		Session session = tester.getSession();

		httpSession.setTemporary(false);
		session.bind();

		String oldId = session.getId();
		assertNotNull(oldId);

		session.changeSessionId();
		String newId = session.getId();

		assertNotEquals(oldId, newId);
	}

	/**
	 * WICKET-6558
	 */
	@Test
	public void lockAfterDetach() throws Exception
	{
		WicketTester tester = new WicketTester(new MockApplication());

		Session session = tester.getSession();

		session.getPageManager();

		session.detach();

		try
		{
			session.getPageManager();
			fail();
		}
		catch (WicketRuntimeException ex)
		{
			assertEquals("The request has been processed. Access to pages is no longer allowed", ex.getMessage());
		}
	}

	private static Stream<String> provideLTRtags() {
		return Stream.of("en", "en-US", "ko", "bg", "ar-Latn", "fa-Cyrl");
	}

	private static Stream<String> provideRTLtags() {
		return Stream.of("ar", "dv", "he", "iw", "fa", "nqo", "ps", "sd", "ug", "ur", "yi", "en-Arab-US", "ru-Hebr", "nl-Thaa", "fi-Nkoo", "fr-Tfng");
	}

	private Session createSessionViaConstructor(String langTag) {
		MockWebRequest rq = new MockWebRequest(Url.parse("/"));
		rq.setLocale(Locale.forLanguageTag(langTag));
		return new WebSession(rq);
	}

	@ParameterizedTest
	@MethodSource("provideLTRtags")
	void testConstructorLtr(String langTag) {
		Session session = createSessionViaConstructor(langTag);
		assertFalse(session.isRtlLocale(), langTag + " should be LTR (left-to-right)");
	}

	@ParameterizedTest
	@MethodSource("provideRTLtags")
	void testConstructorRtl(String langTag) {
		Session session = createSessionViaConstructor(langTag);
		assertTrue(session.isRtlLocale(), langTag + " should be RTL (right-to-left)");
	}

	@ParameterizedTest
	@MethodSource("provideLTRtags")
	void testSetterLtr(String langTag) {
		WicketTester tester = new WicketTester(new MockApplication());
		Session session = tester.getSession();

		session.setLocale(Locale.forLanguageTag(langTag));
		assertFalse(session.isRtlLocale(), langTag + " should be LTR (left-to-right)");
	}

	@ParameterizedTest
	@MethodSource("provideRTLtags")
	void testSetterRtl(String langTag) {
		WicketTester tester = new WicketTester(new MockApplication());
		Session session = tester.getSession();

		session.setLocale(Locale.forLanguageTag(langTag));
		assertTrue(session.isRtlLocale(), langTag + " should be RTL (right-to-left)");
	}
}
