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
package org.apache.wicket.markup.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Attribute parsing of {@link XmlPullParser}. The expected values follow the attribute states of
 * the HTML tokenizer (https://html.spec.whatwg.org/multipage/parsing.html#attribute-name-state),
 * except where the class Javadoc of {@link XmlPullParser} documents a deliberate difference.
 */
class XmlPullParserAttributeTest
{
	static Stream<Arguments> unquotedValues()
	{
		return Stream.of( //
			Arguments.of("<a href=foo/bar.html>", Map.of("href", "foo/bar.html")),
			Arguments.of("<a href=https://example.org/x>", Map.of("href", "https://example.org/x")),
			Arguments.of("<html xmlns:wicket=http://wicket.apache.org>",
				Map.of("xmlns:wicket", "http://wicket.apache.org")),
			Arguments.of("<a href=?a=b&amp;c=d>", Map.of("href", "?a=b&c=d")),
			Arguments.of("<a href=foo/bar.html title=x>",
				Map.of("href", "foo/bar.html", "title", "x")),
			Arguments.of("<a b=c=d>", Map.of("b", "c=d")),
			Arguments.of("<a b=`x`>", Map.of("b", "`x`")),
			Arguments.of("<a b=x<y>", Map.of("b", "x<y")));
	}

	@ParameterizedTest
	@MethodSource("unquotedValues")
	void unquotedValueRunsUntilWhitespace(String markup, Map<String, String> expected)
		throws Exception
	{
		assertEquals(new TreeMap<>(expected), attributesOf(markup));
	}

	static Stream<Arguments> attributeNames()
	{
		return Stream.of( //
			Arguments.of("<button (click)=\"go()\">", Map.of("(click)", "go()")),
			Arguments.of("<input [value]=\"name\">", Map.of("[value]", "name")),
			Arguments.of("<template #default>", Map.of("#default", "")),
			Arguments.of("<div *ngIf=\"shown\">", Map.of("*ngIf", "shown")),
			Arguments.of("<button on:click|once=\"go\">", Map.of("on:click|once", "go")),
			Arguments.of("<button @click.prevent=\"go\">", Map.of("@click.prevent", "go")),
			Arguments.of("<div :class=\"c\">", Map.of(":class", "c")),
			Arguments.of("<div hx-on::after-request=\"go\">",
				Map.of("hx-on::after-request", "go")),
			Arguments.of("<span wicket:id=\"label\">", Map.of("wicket:id", "label")));
	}

	@ParameterizedTest
	@MethodSource("attributeNames")
	void attributeNameKeepsEveryCharacterHtmlAllows(String markup, Map<String, String> expected)
		throws Exception
	{
		assertEquals(new TreeMap<>(expected), attributesOf(markup));
	}

	static Stream<Arguments> recoveredMarkup()
	{
		return Stream.of( //
			Arguments.of("<a b=\"1\"c=\"2\">", Map.of("b", "1", "c", "2")),
			Arguments.of("<tag a=\"1\" \"junk\">", Map.of("a", "1", "\"junk\"", "")),
			Arguments.of("<tag =x>", Map.of("=x", "")),
			Arguments.of("<input a / b>", Map.of("a", "", "b", "")),
			Arguments.of("<a b=>", Map.of("b", "")),
			Arguments.of("<a b= c=d>", Map.of("b", "c=d")),
			Arguments.of("<a b = \"c\">", Map.of("b", "c")));
	}

	@ParameterizedTest
	@MethodSource("recoveredMarkup")
	void markupWithParseErrorsIsReadTheWayTheHtmlTokenizerRecovers(String markup,
		Map<String, String> expected) throws Exception
	{
		assertEquals(new TreeMap<>(expected), attributesOf(markup));
	}

	static Stream<Arguments> quotedValues()
	{
		return Stream.of( //
			Arguments.of("<a title=\" spaced \">", " spaced "),
			Arguments.of("<a title=\"\t\">", "\t"),
			Arguments.of("<a title='a\nb'>", "a\nb"),
			Arguments.of("<a title=\"it's\">", "it's"),
			Arguments.of("<a title='say \"hi\"'>", "say \"hi\""),
			Arguments.of("<a title=\"a &amp; b\">", "a & b"),
			Arguments.of("<a title=\"Next >>\">", "Next >>"));
	}

	@ParameterizedTest
	@MethodSource("quotedValues")
	void quotedValueIsTakenVerbatimApartFromCharacterReferences(String markup, String expected)
		throws Exception
	{
		assertEquals(expected, attributesOf(markup).get("title"));
	}

	@Test
	void backslashDoesNotEscapeTheClosingQuote() throws Exception
	{
		assertEquals(new TreeMap<>(Map.of("title", "C:\\dir\\", "lang", "nl")),
			attributesOf("<a title=\"C:\\dir\\\" lang=nl>"));
	}

	@Test
	void quotedValueMayContainManyClosingBrackets() throws Exception
	{
		String value = ">".repeat(20_000);

		assertEquals(value, attributesOf("<a title=\"" + value + "\">").get("title"));
	}

	@Test
	void slashBeforeTheClosingBracketMakesTheTagOpenClose() throws Exception
	{
		XmlPullParser parser = new XmlPullParser();
		parser.parse("<img src=a.png/>");
		XmlTag tag = parser.nextTag();

		assertTrue(tag.isOpenClose());
		assertEquals("a.png", tag.getAttribute("src").toString());
	}

	@Test
	void attributeWithoutValueHasEmptyValue() throws Exception
	{
		assertEquals(new TreeMap<>(Map.of("disabled", "", "type", "checkbox")),
			attributesOf("<input disabled type=checkbox>"));
	}

	@Test
	void sameAttributeTwiceIsRejected()
	{
		XmlPullParser parser = new XmlPullParser();

		ParseException e = assertThrows(ParseException.class, () -> {
			parser.parse("<a href=x href=\"y\">");
			parser.nextTag();
		});
		assertTrue(e.getMessage().contains("href"), e.getMessage());
	}

	@Test
	void valueWhoseQuoteIsNotClosedInsideTheTagIsRejected()
	{
		XmlPullParser parser = new XmlPullParser();

		ParseException e = assertThrows(ParseException.class, () -> {
			parser.parse("<a x\" b='c\">");
			parser.nextTag();
		});
		assertTrue(e.getMessage().contains("quote"), e.getMessage());
	}

	private static Map<String, String> attributesOf(String markup) throws Exception
	{
		XmlPullParser parser = new XmlPullParser();
		parser.parse(markup);
		XmlTag tag = parser.nextTag();
		Map<String, String> attributes = new TreeMap<>();
		tag.getAttributes().forEach((key, value) -> attributes.put(key, String.valueOf(value)));
		return attributes;
	}
}
