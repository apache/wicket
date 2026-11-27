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
package org.apache.wicket.markup.html.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests that the pages Wicket ships with English text declare English whatever the session locale.
 */
class EnglishPagesLangTest extends WicketTestCase
{
	static Stream<Named<Supplier<Page>>> pages()
	{
		return Stream.of(page("PageExpiredErrorPage", PageExpiredErrorPage::new),
			page("AccessDeniedPage", AccessDeniedPage::new),
			page("InternalErrorPage", InternalErrorPage::new),
			page("ExceptionErrorPage",
				() -> new ExceptionErrorPage(new IllegalStateException("broken"), null)),
			page("BrowserInfoPage", BrowserInfoPage::new));
	}

	static Stream<Arguments> pagesAndLocales()
	{
		return pages().flatMap(
			page -> Stream.of("nl-NL", "ar-EG", "he-IL").map(locale -> Arguments.of(page, locale)));
	}

	private static Named<Supplier<Page>> page(String name, Supplier<Page> page)
	{
		return Named.of(name, page);
	}

	@ParameterizedTest
	@MethodSource("pagesAndLocales")
	void langIsEnglishWithoutDir(Supplier<Page> page, String locale)
	{
		tester.getSession().setLocale(Locale.forLanguageTag(locale));

		tester.startPage(page.get());

		TagTester html = TagTester.createTagByName(tester.getLastResponseAsString(), "html");
		assertEquals("en", html.getAttribute("lang"));
		assertNull(html.getAttribute("dir"));
	}

	@ParameterizedTest
	@MethodSource("pages")
	void doctypeIsHtml5(Supplier<Page> page)
	{
		tester.startPage(page.get());

		assertTrue(tester.getLastResponseAsString().contains("<!DOCTYPE html>\n<html "),
			tester.getLastResponseAsString());
	}
}
