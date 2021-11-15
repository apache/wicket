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
package org.apache.wicket.util.string;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class StringsTest
{
	@Test
	void stripJSessionId() throws Exception
	{
		String url = "http://localhost/abc";
		assertEquals(url, Strings.stripJSessionId(url));
		assertEquals(url + "/", Strings.stripJSessionId(url + "/"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + "?param"));
		assertEquals(url + "?param=a;b", Strings.stripJSessionId(url + "?param=a;b"));
		assertEquals(url + "/?param", Strings.stripJSessionId(url + "/?param"));
		assertEquals(url, Strings.stripJSessionId(url + ";jsessionid=12345"));
		assertEquals(url + "?param", Strings.stripJSessionId(url + ";jsessionid=12345?param"));
		assertEquals(url + "?param=a;b",
			Strings.stripJSessionId(url + ";jsessionid=12345?param=a;b"));

		// WICKET-4816
		assertEquals(url + ";a=b;c=d", Strings.stripJSessionId(url + ";a=b;c=d;jsessionid=12345"));
		assertEquals(url + ";a=b;c=d?param=a;b",
			Strings.stripJSessionId(url + ";a=b;c=d;jsessionid=12345?param=a;b"));

		// WICKET-6858
		final Field sessionIdParamField = Strings.class.getDeclaredField("SESSION_ID_PARAM");
		sessionIdParamField.setAccessible(true);
		try {
			final String customSessionIdParam = ";Custom seSsion - ид=";
			sessionIdParamField.set(null, customSessionIdParam);
			assertEquals(url + ";a=b;c=d?param=a;b",
			             Strings.stripJSessionId(url + ";a=b;c=d" + customSessionIdParam + "12345?param=a;b"));
		} finally {
			sessionIdParamField.set(null, "jsessionid");
			sessionIdParamField.setAccessible(false);
		}
	}

	@Test
	void test()
	{
		assertEquals("foo", Strings.lastPathComponent("bar:garply:foo", ':'));
		assertEquals("foo", Strings.lastPathComponent("foo", ':'));
		assertEquals("bar", Strings.firstPathComponent("bar:garply:foo", ':'));
		assertEquals("foo", Strings.lastPathComponent("foo", ':'));
		assertEquals("garply:foo", Strings.afterFirstPathComponent("bar:garply:foo", ':'));
		assertEquals("", Strings.afterFirstPathComponent("foo", ':'));
		assertEquals("bar:baz", Strings.beforeLast("bar:baz:foo", ':'));
		assertEquals("", Strings.beforeLast("bar", ':'));
		assertEquals("bar", Strings.beforeFirst("bar:baz:foo", ':'));
		assertEquals("", Strings.beforeFirst("bar", ':'));
		assertEquals("baz:foo", Strings.afterFirst("bar:baz:foo", ':'));
		assertEquals("", Strings.afterFirst("bar", ':'));
		assertEquals("foo", Strings.afterLast("bar:baz:foo", ':'));
		assertEquals("", Strings.afterLast("bar", ':'));
		assertEquals("foo", Strings.replaceAll("afaooaaa", "a", "").toString());
		assertEquals("fuzzyffuzzyoofuzzyfuzzyfuzzy",
			Strings.replaceAll("afaooaaa", "a", "fuzzy").toString());
	}

	@Test
	void beforeFirst()
	{
		assertNull(Strings.beforeFirst(null, '.'));
		assertEquals("", Strings.beforeFirst("", '.'));
		assertEquals("", Strings.beforeFirst("", ' '));
		assertEquals("", Strings.beforeFirst(".", '.'));
		assertEquals("", Strings.beforeFirst("..", '.'));
		assertEquals("com", Strings.beforeFirst("com.foo.bar", '.'));
		assertEquals("com", Strings.beforeFirst("com foo bar", ' '));
		assertEquals("com foo", Strings.beforeFirst("com foo.bar", '.'));
	}

	@Test
	void afterFirst()
	{
		assertNull(Strings.afterFirst(null, '.'));
		assertEquals("", Strings.afterFirst("", '.'));
		assertEquals("", Strings.afterFirst("", ' '));
		assertEquals("", Strings.afterFirst(".", '.'));
		assertEquals(".", Strings.afterFirst("..", '.'));
		assertEquals("foo.bar", Strings.afterFirst("com.foo.bar", '.'));
		assertEquals("foo bar", Strings.afterFirst("com foo bar", ' '));
		assertEquals("bar", Strings.afterFirst("com.foo bar", ' '));
	}

	@Test
	void afterLast()
	{
		assertNull(Strings.afterLast(null, '.'));
		assertEquals("", Strings.afterLast("", '.'));
		assertEquals("", Strings.afterLast("", ' '));
		assertEquals("", Strings.afterLast(".", '.'));
		assertEquals("", Strings.afterLast("..", '.'));
		assertEquals("bar", Strings.afterLast("com.foo.bar", '.'));
		assertEquals("bar", Strings.afterLast("com foo bar", ' '));
		assertEquals("bar", Strings.afterLast("com foo.bar", '.'));
	}

	@Test
	void beforeLastPathComponent()
	{
		assertNull(Strings.beforeLastPathComponent(null, '.'));
		assertEquals("", Strings.beforeLastPathComponent("", '.'));
		assertEquals("", Strings.beforeLastPathComponent("", ' '));
		assertEquals("", Strings.beforeLastPathComponent(".", '.'));
		assertEquals(".", Strings.beforeLastPathComponent("..", '.'));
		assertEquals("foo", Strings.beforeLastPathComponent("foo.bar", '.'));
		assertEquals("", Strings.beforeLastPathComponent("foo.bar", ' '));
		assertEquals("foo.ba", Strings.beforeLastPathComponent("foo.bar", 'r'));
		assertEquals("com.foo", Strings.beforeLastPathComponent("com.foo.bar", '.'));
	}

	@Test
	void capitalize()
	{
		assertEquals("Lorem ipsum dolor sit amet",
			Strings.capitalize("lorem ipsum dolor sit amet"));
		assertEquals("Lorem ipsum dolor sit amet",
			Strings.capitalize("Lorem ipsum dolor sit amet"));
		assertEquals(" abcdefghijklm", Strings.capitalize(" abcdefghijklm"));
		assertEquals("", Strings.capitalize(""));
		assertNull(Strings.capitalize(null));
	}

	@Test
	void escapeMarkup()
	{
		assertNull(Strings.escapeMarkup(null));
		assertEquals("", Strings.escapeMarkup("").toString());

		assertEquals("&amp;", Strings.escapeMarkup("&").toString());

		assertEquals("&amp;amp;", Strings.escapeMarkup("&amp;").toString());
		assertEquals("&lt; &gt;&amp;&quot;&#039;?:;{}[]-_+=()*^%$#@!~`",
			Strings.escapeMarkup("< >&\"'?:;{}[]-_+=()*^%$#@!~`").toString());
		assertEquals("&lt;&nbsp;&gt;&amp;&quot;&#039;?:;{}[]-_+=()*^%$#@!~`",
			Strings.escapeMarkup("< >&\"'?:;{}[]-_+=()*^%$#@!~`", true).toString());

		StringBuilder controlCharacters = new StringBuilder();
		StringBuilder controlCharactersEscaped = new StringBuilder();
		for (int i = 0; i < 0x20; i++)
		{
			controlCharacters.append((char)i);
			if (i == 0x09 || i == 0x0a || i == 0x0d)
			{
				controlCharactersEscaped.append((char)i);
			}
			else
			{
				controlCharactersEscaped.append("&#").append(i).append(";");
			}
		}
		assertEquals(controlCharactersEscaped.toString(),
			Strings.escapeMarkup(controlCharacters.toString()).toString());
	}

	@Test
	void escapeMarkupWhiteSpace()
	{
		assertNull(Strings.escapeMarkup(null, true));
		assertEquals("", Strings.escapeMarkup("", true).toString());

		assertEquals("\n \t", Strings.escapeMarkup("\n \t", false).toString());
		assertEquals("\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
			Strings.escapeMarkup("\n \t", true).toString());
		assertEquals("  ", Strings.escapeMarkup("  ", false).toString());
		assertEquals("&nbsp;&nbsp;", Strings.escapeMarkup("  ", true).toString());
	}

	@Test
	void escapeMarkupUnicode()
	{
		assertNull(Strings.escapeMarkup(null, true, true));
		assertEquals("", Strings.escapeMarkup("", true, true).toString());

		// The escaped unicode is ����������"
		assertEquals("&#199;&#252;&#233;&#226;&#228;&#224;&#229;&#231;&#234;&#235;",
			Strings
				.escapeMarkup("\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb", false,
					true)
				.toString());

	}

	@Test
	void replaceHtmlEscapeNumber()
	{
		assertNull(Strings.replaceHtmlEscapeNumber(null));
		assertEquals("", Strings.replaceHtmlEscapeNumber(""));
		assertEquals("abcdefghijklm�", Strings.replaceHtmlEscapeNumber("abcdefghijklm�"));
		assertEquals("a &#", Strings.replaceHtmlEscapeNumber("a &#"));
		assertEquals("\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb",
			Strings.replaceHtmlEscapeNumber(
				"&#199;&#252;&#233;&#226;&#228;&#224;&#229;&#231;&#234;&#235;"));
	}

	@Test
	void firstPathComponent()
	{
		assertNull(Strings.firstPathComponent(null, '.'));
		assertEquals("", Strings.firstPathComponent("", '.'));
		assertEquals("foo", Strings.firstPathComponent("foo", '.'));
		assertEquals("foo", Strings.firstPathComponent("foo.bar", '.'));
		assertEquals("foo bar", Strings.firstPathComponent("foo bar", '.'));
	}

	@Test
	void isEmpty()
	{
		assertTrue(Strings.isEmpty(null));
		assertTrue(Strings.isEmpty(""));
		assertTrue(Strings.isEmpty(" "));
		assertTrue(Strings.isEmpty("           "));
		assertTrue(Strings.isEmpty(" \n\t"));
		assertFalse(Strings.isEmpty("a"));
		assertFalse(Strings.isEmpty(" a"));
	}

	@Test
	void isTrue() throws StringValueConversionException
	{
		assertFalse(Strings.isTrue(null));
		assertFalse(Strings.isTrue(""));
		assertFalse(Strings.isTrue(" \n \t"));

		assertFalse(Strings.isTrue("no"));
		assertFalse(Strings.isTrue("n"));
		assertFalse(Strings.isTrue("false"));
		assertFalse(Strings.isTrue("nO"));
		assertFalse(Strings.isTrue("N"));
		assertFalse(Strings.isTrue("fAlSe"));
		assertFalse(Strings.isTrue("0"));

		assertTrue(Strings.isTrue("yes"));
		assertTrue(Strings.isTrue("y"));
		assertTrue(Strings.isTrue("true"));
		assertTrue(Strings.isTrue("1"));
		assertTrue(Strings.isTrue("YeS"));
		assertTrue(Strings.isTrue("Y"));
		assertTrue(Strings.isTrue("tRuE"));
		assertTrue(Strings.isTrue("1"));
	}

	@Test
	void invalidIsTrue()
	{
		assertThrows(StringValueConversionException.class, () -> {
			Strings.isTrue("foo");
		});

	}

	@Test
	void replaceAll()
	{
		assertNull(Strings.replaceAll(null, null, null));
		assertNull(Strings.replaceAll(null, "", null));
		assertNull(Strings.replaceAll(null, null, ""));
		assertNull(Strings.replaceAll(null, "", ""));

		assertEquals("", Strings.replaceAll("", null, null));
		assertEquals("", Strings.replaceAll("", "", null));
		assertEquals("", Strings.replaceAll("", null, ""));
		assertEquals("", Strings.replaceAll("", "", ""));
		assertEquals("", Strings.replaceAll("", "", "abc"));
		assertEquals("", Strings.replaceAll("", "abc", "def"));
		assertEquals("", Strings.replaceAll("abc", "abc", "").toString());

		assertEquals("abc", Strings.replaceAll("abc", "", ""));
		assertEquals("abc", Strings.replaceAll("abc", "abc", "abc").toString());
		assertEquals("def", Strings.replaceAll("abc", "abc", "def").toString());
		assertEquals("abc", Strings.replaceAll("abc", "ABC", "").toString());

		assertEquals("abc", Strings.replaceAll("abc", "d", null));
		assertEquals("ab", Strings.replaceAll("abc", "c", null).toString());
		assertEquals("bc", Strings.replaceAll("abc", "a", null).toString());

		assertEquals("aaaa", Strings.replaceAll("aa", "a", "aa").toString());

		StringBuilder stringBuilder = new StringBuilder("aa");
		assertEquals("aaaa", Strings.replaceAll(stringBuilder, "a", "aa").toString());

		AppendingStringBuffer asb = new AppendingStringBuffer("aa");
		assertEquals("aaaa", Strings.replaceAll(asb, "a", "aa").toString());
	}

	@Test
	void split()
	{
		assertArrayEquals(new String[0], Strings.split(null, '.'));
		assertArrayEquals(new String[0], Strings.split("", '.'));
		assertArrayEquals(new String[] { "", "" }, Strings.split(".", '.'));
		assertArrayEquals(new String[] { "a", "" }, Strings.split("a.", '.'));
		assertArrayEquals(new String[] { "a", "b" }, Strings.split("a.b", '.'));
		assertArrayEquals(new String[] { "a", "b", "" }, Strings.split("a.b.", '.'));
		assertArrayEquals(new String[] { "", "b", "" }, Strings.split(".b.", '.'));
		assertArrayEquals(new String[] { "a", "b", "c" }, Strings.split("a.b.c", '.'));
		assertArrayEquals(new String[] { "a", "b", "c" }, Strings.split("a b c", ' '));
		assertArrayEquals(new String[] { "abc" }, Strings.split("abc", ' '));
	}

	@Test
	void stripEnding()
	{
		assertNull(Strings.stripEnding(null, null));
		assertEquals("", Strings.stripEnding("", null));
		assertEquals("", Strings.stripEnding("", ""));
		assertEquals("a", Strings.stripEnding("a", ""));
		assertEquals("", Strings.stripEnding("a", "a"));
		assertEquals("a", Strings.stripEnding("a", "aa"));
		assertEquals("abc", Strings.stripEnding("abc", "ab"));
	}

	@Test
	void toBoolean() throws StringValueConversionException
	{
		assertEquals(Boolean.FALSE, Strings.toBoolean(null));
		assertEquals(Boolean.FALSE, Strings.toBoolean("off"));
		assertEquals(Boolean.FALSE, Strings.toBoolean("no"));
		assertEquals(Boolean.FALSE, Strings.toBoolean("n"));
		assertEquals(Boolean.FALSE, Strings.toBoolean("false"));
		assertEquals(Boolean.FALSE, Strings.toBoolean("0"));

		assertEquals(Boolean.TRUE, Strings.toBoolean("on"));
		assertEquals(Boolean.TRUE, Strings.toBoolean("yes"));
		assertEquals(Boolean.TRUE, Strings.toBoolean("y"));
		assertEquals(Boolean.TRUE, Strings.toBoolean("true"));
		assertEquals(Boolean.TRUE, Strings.toBoolean("1"));
	}

	@Test
	void invalidToBoolean()
	{
		assertThrows(StringValueConversionException.class, () -> {
			Strings.toBoolean("waar");
		});
	}

	@Test
	void toChar()
	{
		assertEquals(' ', Strings.toChar(" "));
		assertEquals('a', Strings.toChar("a"));
	}

	@Test
	void invalidToChar1()
	{
		assertThrows(StringValueConversionException.class, () -> {
			Strings.toChar("");
		});

	}

	@Test
	void invalidToChar2()
	{

		assertThrows(StringValueConversionException.class, () -> {
			Strings.toChar(null);
		});
	}

	@Test
	void invalidToChar3()
	{
		assertThrows(StringValueConversionException.class, () -> {
			Strings.toChar("aa");
		});
	}

	@Test
	void toMultilineMarkup()
	{
		assertNull(Strings.toMultilineMarkup(null));
		assertEquals("<p></p>", Strings.toMultilineMarkup("").toString());
		assertEquals("<p></p><p></p>", Strings.toMultilineMarkup("\n\n").toString());
		assertEquals("<p><br/></p>", Strings.toMultilineMarkup("\n").toString());
		assertEquals("<p>abc</p>", Strings.toMultilineMarkup("abc").toString());
		assertEquals("<p>abc<br/></p>", Strings.toMultilineMarkup("abc\n").toString());
		assertEquals("<p>abc<br/>def</p>", Strings.toMultilineMarkup("abc\ndef").toString());
		assertEquals("<p>abc<br/>def</p>", Strings.toMultilineMarkup("abc\r\ndef").toString());
		assertEquals("<p>abc<br/>def<br/>ghi</p>",
			Strings.toMultilineMarkup("abc\ndef\nghi").toString());

		// WICKET-4837
		assertEquals(
			"<p><a href=\"mailto:john@doe.com\">john@doe.com</a><br/><a href=\"http://apache.wicket.org\">http://apache.wicket.org</a></p>",
			Strings.toMultilineMarkup(
				"<a href=\"mailto:john@doe.com\">john@doe.com</a>\n<a href=\"http://apache.wicket.org\">http://apache.wicket.org</a>")
				.toString());

		assertEquals("<p>abc</p><p>def</p><p>ghi</p>",
			Strings.toMultilineMarkup("abc\n\ndef\n\nghi").toString());
		assertEquals("<p>abc</p><p>def</p><p>ghi</p>",
			Strings.toMultilineMarkup("abc\r\n\r\ndef\r\n\r\nghi").toString());
		assertEquals("<p>abc</p><p>def</p><p>ghi</p><p></p>",
			Strings.toMultilineMarkup("abc\r\n\r\ndef\r\n\r\nghi\n\n").toString());

		assertEquals("<p>\\n</p>", Strings.toMultilineMarkup("\\n").toString());
		assertEquals("<p>a\\nbc</p>", Strings.toMultilineMarkup("a\\nbc").toString());
	}

	@Test
	void testToString()
	{
		assertNull(Strings.toString((Object)null));
		assertEquals("", Strings.toString(""));

		assertEquals("<Null Throwable>", Strings.toString(null));
		try
		{
			throw new IllegalArgumentException("Foo");
		}
		catch (IllegalArgumentException e)
		{
			final String toString = Strings.toString((Object)e);
			String before = Strings.beforeFirst(toString, '\n').trim();
			assertEquals("Message: Foo", before);
		}
	}

	@Test
	void toStringThrowable()
	{
		NullPointerException np = new NullPointerException("null test");
		RuntimeException wre = new RuntimeException("null test", np);
		String exceptionString = Strings.toString(wre);
		assertTrue(exceptionString.length() > 1);
		assertTrue(exceptionString.contains("RuntimeException"));
	}

	@Test
	void testJoin() throws Exception
	{
		List<String> fragments = Arrays.asList("foo", "bar", "baz");

		assertEquals("foo-bar-baz", Strings.join("-", fragments));

		// empty separator
		assertEquals("foobarbaz", Strings.join("", fragments));
	}

	@Test
	void testNonchar()
	{
		assertEquals("", Strings.escapeMarkup("\ufffe\uFDDF\uFDE0\uFDD0\uFDEF").toString());
		assertEquals("", Strings.toEscapedUnicode("\ufffe\uFDDF\uFDE0\uFDD0\uFDEF"));
	}
}
