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
package wicket.markup.parser;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import junit.framework.TestCase;
import wicket.markup.MarkupElement;

/**
 * Quite some tests are already with MarkupParser.
 * 
 * @author Juergen Donnerstag
 */
public class XmlPullParserTest extends TestCase
{
	/**
	 * 
	 * @throws Exception
	 */
	public final void testBasics() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("This is a text");
		MarkupElement elem = parser.nextTag();
		assertNull(elem);

		parser.parse("<tag/>");
		XmlTag tag = (XmlTag)parser.nextTag();
		assertFalse(tag.isOpen());
		// assertFalse(tag.isOpen("tag"));
		// assertFalse(tag.isOpen("xxx"));
		assertFalse(tag.isClose());
		assertTrue(tag.isOpenClose());
		// assertTrue(tag.isOpenClose("tag"));
		assertEquals("tag", tag.getName());
		assertNull(tag.getNamespace());
		assertEquals(0, tag.getAttributes().size());

		// extra spaces
		parser.parse("<tag ></tag >");
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isOpen());
		// assertTrue(tag.isOpen("tag"));
		// assertFalse(tag.isOpen("xxx"));
		assertFalse(tag.isClose());
		assertFalse(tag.isOpenClose());
		// assertFalse(tag.isOpenClose("tag"));
		assertEquals("tag", tag.getName());
		assertNull(tag.getNamespace());
		assertEquals(0, tag.getAttributes().size());

		tag = (XmlTag)parser.nextTag();
		assertFalse(tag.isOpen());
		// assertFalse(tag.isOpen("tag"));
		// assertFalse(tag.isOpen("xxx"));
		assertTrue(tag.isClose());
		assertFalse(tag.isOpenClose());
		// assertFalse(tag.isOpenClose("tag"));
		assertEquals("tag", tag.getName());
		assertNull(tag.getNamespace());
		assertEquals(0, tag.getAttributes().size());

		parser.parse("<tag>  </tag>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());

		parser.parse("xx <tag> yy </tag> zz");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());

		// XmlPullParser does NOT check that tags get properly closed
		parser.parse("<tag>");
		tag = (XmlTag)parser.nextTag();
		tag = (XmlTag)parser.nextTag();
		assertNull(elem);

		parser.parse("<tag> <tag> <tag>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));

		parser.parse("<ns:tag/>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpenClose("tag"));
		assertEquals("ns", tag.getNamespace());
		assertEquals("tag", tag.getName());

		parser.parse("<ns:tag></ns:tag>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		assertEquals("ns", tag.getNamespace());
		assertEquals("tag", tag.getName());

		XmlTag closeTag = (XmlTag)parser.nextTag();
		assertTrue(closeTag.isClose());
		assertEquals("ns", closeTag.getNamespace());
		assertEquals("tag", closeTag.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testEncoding() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>");
		assertEquals("iso-8859-1", parser.getEncoding());
		XmlTag tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		parser.parse("<?xml version=\"1.0\" encoding='iso-8859-1' ?> test test");
		assertEquals("iso-8859-1", parser.getEncoding());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		// re-order and move close (remove whitespaces
		parser.parse("   <?xml encoding='iso-8859-1'version=\"1.0\"?> test test");
		assertEquals("iso-8859-1", parser.getEncoding());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		// attribute value must be enclosed by ""
		parser.parse("<?xml encoding=iso-8859-1 ?> test test");
		assertEquals("iso-8859-1", parser.getEncoding());

		// Invaluid encoding
		Exception ex = null;
		try
		{
			parser.parse("<?xml encoding='XXX' ?>");
		}
		catch (UnsupportedEncodingException e)
		{
			ex = e;
		}
		assertNotNull(ex);

		// no extra characters allowed before <?xml>
		// TODO General: I'd certainly prefer an exception
		parser.parse("xxxx <?xml encoding='iso-8859-1' ?>");
		assertNull(parser.getEncoding());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		// no extra characters allowed before <?xml>
		// Are comments allowed preceding the encoding string?
		parser.parse("<!-- Comment --> <?xml encoding='iso-8859-1' ?>");
		assertNull(parser.getEncoding());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		// 'test' is not a valid attribut. But we currently don't test it.
		parser.parse("<?xml test='123' >");
		assertNull(parser.getEncoding());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testAttributes() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<tag>");
		XmlTag tag = (XmlTag)parser.nextTag();
		assertEquals(0, tag.getAttributes().size());
		// assertTrue(tag.isOpen("tag"));
		assertFalse(tag.getAttributes().containsKey("attr"));
		assertNull(tag.getAttributes().getString("attr"));

		parser.parse("<tag attr='1234'>");
		tag = (XmlTag)parser.nextTag();
		assertEquals(1, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr"));
		assertEquals("1234", tag.getAttributes().getString("attr"));

		parser.parse("<tag attr=1234>");
		tag = (XmlTag)parser.nextTag();
		assertEquals(1, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr"));
		assertEquals("1234", tag.getAttributes().getString("attr"));

		parser.parse("<tag attr=1234 >");
		tag = (XmlTag)parser.nextTag();
		assertEquals(1, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr"));
		assertEquals("1234", tag.getAttributes().getString("attr"));

		parser.parse("<tag attr-withHypen=1234 >");
		tag = (XmlTag)parser.nextTag();
		assertEquals(1, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr-withHypen"));
		assertEquals("1234", tag.getAttributes().getString("attr-withHypen"));

		parser.parse("<tag attr=\"1234\">");
		tag = (XmlTag)parser.nextTag();
		assertEquals(1, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr"));
		assertEquals("1234", tag.getAttributes().getString("attr"));

		parser.parse("<tag attr='1234' test='23'>");
		tag = (XmlTag)parser.nextTag();
		assertEquals(2, tag.getAttributes().size());
		assertTrue(tag.getAttributes().containsKey("attr"));
		assertEquals("1234", tag.getAttributes().getString("attr"));
		assertTrue(tag.getAttributes().containsKey("test"));
		assertEquals("23", tag.getAttributes().getString("test"));

		parser.parse("<tag attr='1234' attr='23'>");
		Exception ex = null;
		try
		{
			tag = (XmlTag)parser.nextTag();
		}
		catch (ParseException e)
		{
			ex = e;
		}
		assertNotNull(ex);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testComments() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<!-- test --><tag>");
		XmlTag tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));

		parser.parse("<!-- test --><tag> aaa <!-- test 1 --> bbb <tag> <!-- test --> </tag>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);

		// As you can see, XmlPullParser is really a shallow parser only
		parser.parse("<!-- test --><tag> aaa <?tag test 1 ?> bbb <tag> <!DOCTYPE test > </tag>");
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		// assertTrue(tag.isOpen("tag"));
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testCompressWhitespace() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testScript() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<html><script language=\"JavaScript\">... <x a> ...</script></html>");
		XmlTag tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isOpen());
		assertEquals("html", tag.getName());
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isOpen());
		assertEquals("script", tag.getName());
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());
		assertEquals("script", tag.getName());
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());
		assertEquals("html", tag.getName());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testConditionalComments() throws Exception
	{
		final XmlPullParser parser = new XmlPullParser();
		parser.parse("<!--[if IE]><a href='test.html'>my link</a><![endif]-->");
		XmlTag tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isOpen());
		assertEquals("a", tag.getName());
		tag = (XmlTag)parser.nextTag();
		assertTrue(tag.isClose());
		assertEquals("a", tag.getName());
		tag = (XmlTag)parser.nextTag();
		assertNull(tag);
	}
}
