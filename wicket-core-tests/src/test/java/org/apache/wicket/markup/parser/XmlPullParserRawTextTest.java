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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.parser.IXmlPullParser.HttpTagType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The body of {@code <script>} and {@code <style>} is raw text: {@link XmlPullParser} reports it as
 * one {@link HttpTagType#BODY} and does not look for tags in it. A script whose type is not a
 * script or data type is a template and is parsed as markup (WICKET-5288).
 */
class XmlPullParserRawTextTest
{
	private static final String CODE = "if (a<b && c>d) go();";

	@ParameterizedTest
	@ValueSource(strings = { "<script>", "<script type=\"\">", "<script type=\" \">",
			"<script type=>", "<script type=\"text/javascript\">", "<script type=text/javascript>",
			"<script type=\"TEXT/JavaScript\">", "<script type=\"text/javascript; charset=utf-8\">",
			"<script type=\"application/javascript\">", "<script type=\"application/ecmascript\">",
			"<script type=\"module\">", "<script type=\"importmap\">",
			"<script type=\"speculationrules\">", "<script type=\"application/json\">",
			"<script type=\"application/ld+json\">", "<script data-type=\"x\">",
			"<script src=\"x.js?type=x\">", "<script language=\"JavaScript\">" })
	void scriptOfAScriptOrDataTypeHasRawTextBody(String openTag) throws Exception
	{
		assertEquals(List.of("script", "BODY " + CODE, "/script"),
			tokens(openTag + CODE + "</script>"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "<script type=\"text/x-handlebars-template\">",
			"<script type=\"text/template\">" })
	void scriptOfAnotherTypeIsATemplateWhoseTagsAreParsed(String openTag) throws Exception
	{
		assertEquals(List.of("script", "span", "BODY x", "/span", "/script"),
			tokens(openTag + "<span>x</span></script>"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "<style>", "<style type=\"text/css\">" })
	void styleHasRawTextBody(String openTag) throws Exception
	{
		assertEquals(List.of("style", "BODY /* <b> */", "/style"),
			tokens(openTag + "/* <b> */</style>"));
	}

	@Test
	void rawTextElementNameIsCaseInsensitive() throws Exception
	{
		assertEquals(List.of("STYLE", "BODY /* <b> */", "/STYLE"),
			tokens("<STYLE>/* <b> */</STYLE>"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "script-loader", "scripts", "styled-box", "stylesheet" })
	void elementWhoseNameOnlyStartsWithScriptOrStyleIsParsedAsMarkup(String name)
		throws Exception
	{
		assertEquals(List.of(name, "span", "BODY x", "/span", "/" + name),
			tokens("<" + name + "><span>x</span></" + name + ">"));
	}

	@Test
	void rawTextEndsOnlyAtTheFullEndTagName() throws Exception
	{
		assertEquals(List.of("script", "BODY var s = '</scripts>';", "/script"),
			tokens("<script>var s = '</scripts>';</script>"));
	}

	@Test
	void rawTextEndTagIsCaseInsensitiveAndMayHaveWhitespace() throws Exception
	{
		assertEquals(List.of("script", "BODY " + CODE, "/SCRIPT"),
			tokens("<script>" + CODE + "</SCRIPT >"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "don't", "say \"hi\"", "a > b", "" })
	void cdataSectionEndsAtItsTerminator(String content) throws Exception
	{
		assertEquals(List.of("p", "CDATA", "/p"),
			tokens("<p><![CDATA[" + content + "]]></p>"));
	}

	private static List<String> tokens(String markup) throws Exception
	{
		XmlPullParser parser = new XmlPullParser();
		parser.parse(markup);
		List<String> tokens = new ArrayList<>();
		while (true)
		{
			parser.setPositionMarker();
			HttpTagType type = parser.next();
			if (type == HttpTagType.NOT_INITIALIZED)
			{
				return tokens;
			}
			if (type == HttpTagType.TAG)
			{
				XmlTag tag = parser.getElement();
				tokens.add((tag.isClose() ? "/" : "") + tag.getName());
			}
			else if (type == HttpTagType.BODY)
			{
				tokens.add(type + " " + parser.getString());
			}
			else
			{
				tokens.add(type.name());
			}
		}
	}
}
