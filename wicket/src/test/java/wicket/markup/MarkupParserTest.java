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
package wicket.markup;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.WicketTestCase;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.resource.StringResourceStream;
import wicket.util.resource.locator.ClassLoaderResourceStreamLocator;
import wicket.util.resource.locator.IResourceStreamLocator;
import wicket.util.string.StringValueConversionException;

/**
 * Test cases for markup parser.
 * 
 * @author Jonathan Locke
 */
public final class MarkupParserTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(MarkupParserTest.class);

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public MarkupParserTest(String name)
	{
		super(name);
	}

	/**
	 * Helper
	 * 
	 * @param namespace
	 * @param markup
	 * @return IMarkup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	private MarkupFragment parse(final String namespace, final String markup) throws IOException,
			ResourceStreamNotFoundException
	{
		MarkupResourceStream stream = new MarkupResourceStream(new StringResourceStream(markup),
				null, null);
		final MarkupParser parser = new MarkupParserFactory()
				.newMarkupParser(stream);
		parser.setWicketNamespace(namespace);
		return parser.readAndParse();
	}

	/**
	 * Helper
	 * 
	 * @param markup
	 * @return IMarkup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	private MarkupFragment parse(final String markup) throws IOException,
			ResourceStreamNotFoundException
	{
		MarkupResourceStream stream = new MarkupResourceStream(new StringResourceStream(markup),
				null, null);
		final MarkupParser parser = new MarkupParserFactory()
				.newMarkupParser(stream);
		return parser.readAndParse();
	}

	/**
	 * Helper
	 * 
	 * @param locator
	 * @param c
	 * @param style
	 * @param locale
	 * @param extension
	 * @return MarkupResourceStream
	 */
	private MarkupResourceStream newMarkupResourceStream(final IResourceStreamLocator locator,
			final Class c, final String style, final Locale locale, final String extension)
	{
		IResourceStream resource = locator.locate(c, c.getName().replace('.', '/'), style, locale,
				extension);
		MarkupResourceStream res = new MarkupResourceStream(resource, null, null);
		return res;
	}

	/**
	 * Helper
	 * 
	 * @param namespace
	 * @param resource
	 * @return IMarkup
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	private MarkupFragment parse(final MarkupResourceStream resource) throws IOException,
			ResourceStreamNotFoundException
	{
		final MarkupParser parser = new MarkupParserFactory()
				.newMarkupParser(resource);
		return parser.readAndParse();
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 * @throws Exception
	 */
	public final void testTagParsing() throws Exception
	{
		final MarkupFragment markup = parse(
				"componentName",
				"This is a test <a componentName:id=\"a\" href=\"foo.html\"> <b componentName:id=\"b\">Bold!</b> "
						+ "<img componentName:id=\"img\" width=9 height=10 src=\"foo\"> <marker componentName:id=\"marker\"/> </a>");

		final MarkupStream markupStream = new MarkupStream(markup);
		final ComponentTag aOpen = (ComponentTag)markupStream.next();

		log.info(aOpen.toString());
		Assert.assertTrue(aOpen.getName().equals("a"));
		Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

		markupStream.next();

		final ComponentTag boldOpen = (ComponentTag)markupStream.next();

		log.info(boldOpen.toString());
		Assert.assertTrue(boldOpen.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.OPEN, boldOpen.getType());

		markupStream.next();

		final ComponentTag boldClose = (ComponentTag)markupStream.next();

		log.info(boldClose.toString());
		Assert.assertTrue(boldClose.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.CLOSE, boldClose.getType());

		markupStream.next();

		final ComponentTag img = (ComponentTag)markupStream.next();

		log.info(img.toString());
		Assert.assertTrue(img.getName().equals("img"));
		Assert.assertEquals(9, img.getAttributes().getInt("width"));
		Assert.assertEquals(10, img.getAttributes().getInt("height"));
		Assert.assertEquals(XmlTag.Type.OPEN, img.getType());

		markupStream.next();

		final ComponentTag marker = (ComponentTag)markupStream.next();

		log.info(marker.toString());
		Assert.assertTrue(marker.getName().equals("marker"));
		Assert.assertEquals(XmlTag.Type.OPEN_CLOSE, marker.getType());

		markupStream.next();

		final ComponentTag aClose = (ComponentTag)markupStream.next();

		log.info(aClose.toString());
		Assert.assertTrue(aClose.getName().equals("a"));
	}

	/**
	 * 
	 * @throws StringValueConversionException
	 * @throws Exception
	 */
	public final void testTagParsingFragments() throws Exception
	{
		final MarkupFragment markup = parse(
				"componentName",
				"This is a test <a componentName:id=\"a\" href=\"foo.html\"> <b componentName:id=\"b\">Bold!</b> "
						+ "<img componentName:id=\"img\" width=9 height=10 src=\"foo\"> <marker componentName:id=\"marker\"/> </a>");

		final List<MarkupElement> elems = markup.getAllElementsFlat();
		final Iterator<MarkupElement> markupStream = elems.iterator();
		markupStream.next();
		final ComponentTag aOpen = (ComponentTag)markupStream.next();

		log.info(aOpen.toString());
		Assert.assertTrue(aOpen.getName().equals("a"));
		Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

		markupStream.next();

		final ComponentTag boldOpen = (ComponentTag)markupStream.next();

		log.info(boldOpen.toString());
		Assert.assertTrue(boldOpen.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.OPEN, boldOpen.getType());

		markupStream.next();

		final ComponentTag boldClose = (ComponentTag)markupStream.next();

		log.info(boldClose.toString());
		Assert.assertTrue(boldClose.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.CLOSE, boldClose.getType());

		markupStream.next();

		final ComponentTag img = (ComponentTag)markupStream.next();

		log.info(img.toString());
		Assert.assertTrue(img.getName().equals("img"));
		Assert.assertEquals(9, img.getAttributes().getInt("width"));
		Assert.assertEquals(10, img.getAttributes().getInt("height"));
		Assert.assertEquals(XmlTag.Type.OPEN, img.getType());

		markupStream.next();

		final ComponentTag marker = (ComponentTag)markupStream.next();

		log.info(marker.toString());
		Assert.assertTrue(marker.getName().equals("marker"));
		Assert.assertEquals(XmlTag.Type.OPEN_CLOSE, marker.getType());

		markupStream.next();

		final ComponentTag aClose = (ComponentTag)markupStream.next();

		log.info(aClose.toString());
		Assert.assertTrue(aClose.getName().equals("a"));
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void test() throws Exception
	{
		MarkupFragment fragment = parse(
				"componentName",
				"This is a test <a componentName:id=9> <b>bold</b> <b componentName:id=10/></a> of the emergency broadcasting system");

		List<MarkupElement> tokens = fragment.getAllElementsFlat();
		log.info("tok(0)=" + tokens.get(0));
		log.info("tok(1)=" + tokens.get(1));
		log.info("tok(2)=" + tokens.get(2));
		log.info("tok(3)=" + tokens.get(3));
		log.info("tok(4)=" + tokens.get(4));
		log.info("tok(5)=" + tokens.get(5));

		Assert.assertTrue(tokens.get(0).equals("This is a test "));

		final ComponentTag a = (ComponentTag)tokens.get(1);

		Assert.assertEquals(9, a.getAttributes().getInt("componentName:id"));
		Assert.assertTrue(tokens.get(2).equals(" <b>bold</b> "));

		final ComponentTag b = (ComponentTag)tokens.get(3);

		Assert.assertEquals(10, b.getAttributes().getInt("componentName:id"));

		final ComponentTag closeA = (ComponentTag)tokens.get(4);

		Assert.assertEquals("a", closeA.getName());
		Assert.assertTrue(tokens.get(5).equals(" of the emergency broadcasting system"));
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void testXhtmlDocument() throws Exception
	{
		final String docText = ""
				+ "<?xml version='1.0' encoding='iso-8859-1' ?>"
				+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
				+ "<html>" + "<head><title>Some Page</title></head>"
				+ "<body><h1>XHTML Test</h1></body>" + "</html>";
		final MarkupFragment tokens = parse("componentName", docText);

		log.info("tok(0)=" + tokens.get(0));
		Assert.assertEquals(docText.substring(44), tokens.get(0).toString());
	}

	/**
	 * 
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	public final void testFileDocument() throws ParseException, ResourceStreamNotFoundException,
			IOException
	{
		IResourceStreamLocator locator = new ClassLoaderResourceStreamLocator();
		MarkupResourceStream resource = newMarkupResourceStream(locator, this.getClass(), "1",
				null, "html");

		MarkupFragment tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "2", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "3", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "4", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		// File from jar (URL resource)
		resource = newMarkupResourceStream(locator, PageExpiredErrorPage.class, null, null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "5", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		// wicket:param is no longer supported
		// resource = newMarkupResourceStream(locator, this.getClass(), "6",
		// null, "html");
		// tokens = parse(resource);
		// log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "7", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "8", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "9", null, "html");
		tokens = parse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "9", null, "html");
		MarkupParser parser = new MarkupParserFactory()
				.newMarkupParser(resource);
		parser.setStripComments(true);
		parser.readAndParse();
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());
	}

	/**
	 * Test &lt;wicket: .
	 * 
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	public final void testWicketTag() throws ParseException, ResourceStreamNotFoundException,
			IOException
	{
		WicketTagIdentifier.registerWellKnownTagName("body");
		WicketTagIdentifier.registerWellKnownTagName("border");
		WicketTagIdentifier.registerWellKnownTagName("panel");

		parse("<span wicket:id=\"test\"/>");
		parse("<span wicket:id=\"test\">Body</span>");
		parse("This is a test <span wicket:id=\"test\"/>");
		parse("This is a test <span wicket:id=\"test\">Body</span>");
		parse("<a wicket:id=\"[autolink]\" href=\"test.html\">Home</a>");

		parse("<wicket:body/>");
		parse("<wicket:border/>");
		parse("<wicket:panel/>");

		try
		{
			parse("<wicket:remove/>");
			assertTrue("Should have thrown an exception", false);
		}
		catch (MarkupException ex)
		{
			// ignore
		}

		MarkupFragment markup = parse("<wicket:remove>  </wicket:remove>");
		assertEquals(0, markup.size());

		markup = parse("<wicket:remove> <span id=\"test\"/> </wicket:remove>");
		assertEquals(0, markup.size());

		markup = parse("<div><wicket:remove> <span id=\"test\"/> </wicket:remove></div>");
		assertEquals(2, markup.size());
		assertEquals("<div>", ((RawMarkup)markup.get(0)).toString());
		assertEquals("</div>", ((RawMarkup)markup.get(1)).toString());

		try
		{
			parse("<wicket:remove> <wicket:remove> </wicket:remove> </wicket:remove>");
			assertTrue(
					"Should have thrown an exception: remove regions must not contain wicket-components",
					false);
		}
		catch (MarkupException ex)
		{
			// ignore
		}

		parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\"/>");
		parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">    </wicket:component>");
		parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">  <span wicket:id=\"msg\">hello world!</span></wicket:component>");
		parse("<wicket:panel><div id=\"definitionsContentBox\"><span wicket:id=\"contentPanel\"/></div></wicket:panel>");
	}

	/**
	 * Test &lt;wicket: .
	 * 
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	public final void testDefaultWicketTag() throws ParseException,
			ResourceStreamNotFoundException, IOException
	{
		MarkupFragment markup = parse("wcn", "<span wcn:id=\"test\"/>");
		assertEquals(1, markup.size());

		markup = parse("wcn", "<span wicket:id=\"test\"/>");
		assertEquals(1, markup.size());

		WicketTagIdentifier.registerWellKnownTagName("xxx");
		markup = parse("wcn", "<wcn:xxx>  </wcn:xxx>");
		assertEquals(3, markup.size());
	}

	/**
	 * Test &lt;wicket: .
	 * 
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	public final void testScript() throws ParseException, ResourceStreamNotFoundException,
			IOException
	{
		MarkupFragment markup = parse("<html wicket:id=\"test\"><script language=\"JavaScript\">... <x a> ...</script></html>");
		assertEquals(3, markup.size());
		assertEquals("html", ((ComponentTag)markup.get(0)).getName());
		assertEquals("html", ((ComponentTag)markup.get(2)).getName());
		assertEquals(true, markup.get(1) instanceof RawMarkup);
		assertEquals("<script language=\"JavaScript\">... <x a> ...</script>", ((RawMarkup)markup
				.get(1)).toString());
	}

	/**
	 * Test &lt;wicket: .
	 * 
	 * @throws ParseException
	 * @throws ResourceStreamNotFoundException
	 * @throws IOException
	 */
	public final void testCDATA() throws ParseException, ResourceStreamNotFoundException,
			IOException
	{
		MarkupFragment markup = parse("<html><![CDATA[ test ]]></html>");
		assertEquals(1, markup.size());
		// assertEquals("html", ((ComponentTag)markup.get(0)).getName());
		// assertEquals("html", ((ComponentTag)markup.get(1)).getName());
		// assertEquals(true, markup.get(1) instanceof RawMarkup);
		// assertEquals("<![CDATA[ test ]]>",
		// ((RawMarkup)markup.get(1)).toString());
		assertEquals(true, markup.get(0) instanceof RawMarkup);
		assertEquals("<html><![CDATA[ test ]]></html>", ((RawMarkup)markup.get(0)).toString());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public final void testBalancing() throws IOException, ResourceStreamNotFoundException
	{
		// Note: <img> is one of these none-balanced HTML tags
		MarkupFragment markup = parse("<span wicket:id=\"span\"><img wicket:id=\"img\"><span wicket:id=\"span2\"></span></span>");

		ComponentTag t = markup.getTag();
		assertEquals(t.getId(), "span");
		assertNotNull(markup.getChildFragment("span", false));

		MarkupFragment fragment = (MarkupFragment)markup.get(1);
		assertEquals(fragment.getId(), "img");
		assertNotNull(markup.getChildFragment("span:img", false));

		fragment = (MarkupFragment)markup.get(2);
		assertEquals(fragment.getId(), "span2");
		assertNotNull(markup.getChildFragment("span:span2", false));
	}
}
