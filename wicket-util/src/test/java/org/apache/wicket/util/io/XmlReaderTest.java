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
package org.apache.wicket.util.io;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("javadoc")
public class XmlReaderTest
{
	@Test
	public void readHtmlFileWithoutAnyTags() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_1.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("Zeile 1", bufReader.readLine());

			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithHtmlAndBody() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_2.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("<html>", bufReader.readLine());
			assertEquals("<body>", bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleSansVersionAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_3.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_4.html"), null);
		assertNull(reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_5.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingInSingleQuotesAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_6.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithXmlPreambleWithVersionAndEncodingSansQuotesAndHtmlTag() throws Exception
	{
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_7.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtmlFileWithBomAndXmlPreambleWithEncodingSansQuotesAndHtmlTag() throws Exception
	{
		// test_8.html starts with <U+FEFF> character
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_8.html"), null);
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("", bufReader.readLine().trim());
			assertEquals("<html>", bufReader.readLine());
			assertNull(bufReader.readLine());
		}
	}

	@Test
	public void readHtml5FileWithMetaCharset() throws Exception
	{
		// test_9.html is saved as UTF-8 and declares it only with <meta charset="UTF-8">
		XmlReader reader = new XmlReader(this.getClass().getResourceAsStream("test_9.html"),
			"ISO-8859-1");
		assertEquals("UTF-8", reader.getEncoding());

		try (BufferedReader bufReader = new BufferedReader(reader);)
		{
			assertEquals("<!DOCTYPE html>", bufReader.readLine());
			assertEquals("<html>", bufReader.readLine());
			assertEquals("<head>", bufReader.readLine());
			assertEquals("<meta charset=\"UTF-8\">", bufReader.readLine());
			assertEquals("</head>", bufReader.readLine());
			assertEquals("<body>Größe: 5 €</body>", bufReader.readLine());
		}
	}

	@Test
	public void metaCharsetIsCaseInsensitiveAndMayBeUnquoted() throws Exception
	{
		assertEquals("iso-8859-1", encodingOf("<!DOCTYPE html><HEAD><META CHARSET=iso-8859-1>"));
	}

	@Test
	public void metaCharsetMayFollowOtherAttributesInSingleQuotes() throws Exception
	{
		assertEquals("UTF-8", encodingOf("<head><meta name='x' charset = 'UTF-8' />"));
	}

	@Test
	public void metaHttpEquivContentTypeDeclaresCharset() throws Exception
	{
		assertEquals("windows-1252", encodingOf(
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">"));
	}

	@Test
	public void xmlDeclarationWinsOverMetaCharset() throws Exception
	{
		assertEquals("UTF-8", encodingOf(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<meta charset=\"ISO-8859-1\">"));
	}

	@Test
	public void firstMetaCharsetWins() throws Exception
	{
		assertEquals("UTF-8", encodingOf("<meta charset=\"UTF-8\"><meta charset=\"ISO-8859-1\">"));
	}

	@Test
	public void metaCharsetInCommentIsIgnored() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<!-- <meta charset=\"UTF-8\"> --><p>"));
	}

	@Test
	public void metaCharsetInsideAnotherTagsAttributeIsIgnored() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<div title=\"<meta charset=UTF-8>\"></div>"));
	}

	@Test
	public void otherAttributeEndingInCharsetIsIgnored() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<meta data-charset=\"UTF-8\">"));
	}

	@Test
	public void unsupportedMetaCharsetIsSkipped() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<meta charset=\"no-such-charset\">"));
		assertEquals("UTF-8",
			encodingOf("<meta charset=\"no such charset\"><meta charset=\"UTF-8\">"));
	}

	@Test
	public void metaCharsetAfterTheFirst1024BytesIsIgnored() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<p>" + "x".repeat(1024) + "<meta charset=UTF-8>"));
	}

	@Test
	public void metaCharsetCutOffByThe1024ByteLimitIsIgnored() throws Exception
	{
		String tag = "<meta charset=\"UTF-8\">";
		String padding = "x".repeat(1024 - tag.length() + 3);
		assertEquals(DEFAULT_ENCODING, encodingOf(padding + tag));
	}

	@Test
	public void metaCharsetEndingExactlyAtTheLimitIsFound() throws Exception
	{
		String tag = "<meta charset=\"UTF-8\">";
		assertEquals("UTF-8", encodingOf("x".repeat(1024 - tag.length()) + tag));
	}

	@Test
	public void markupWithoutDeclarationUsesTheDefault() throws Exception
	{
		assertEquals(DEFAULT_ENCODING, encodingOf("<html><head><title>x</title></head>"));
	}

	private static final String DEFAULT_ENCODING = "US-ASCII";

	private static String encodingOf(String markup) throws IOException
	{
		byte[] bytes = markup.getBytes(StandardCharsets.ISO_8859_1);
		try (XmlReader reader = new XmlReader(new ByteArrayInputStream(bytes), DEFAULT_ENCODING))
		{
			return reader.getEncoding();
		}
	}
}
