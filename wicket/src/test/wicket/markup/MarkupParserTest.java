/*
 * $Id: MarkupParserTest.java 5662 2006-05-05 18:12:51 +0000 (Fri, 05 May 2006)
 * jannehietamaki $ $Revision$ $Date: 2006-05-05 18:12:51 +0000 (Fri, 05
 * May 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketTestCase;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.XmlTag;
import wicket.markup.parser.filter.WicketTagIdentifier;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
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
	private static final Log log = LogFactory.getLog(MarkupParserTest.class);

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
	 * 
	 * @throws StringValueConversionException
	 * @throws Exception
	 */
	public final void testTagParsing() throws Exception
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.setWicketNamespace("componentName");

		final IMarkup markup = parser
				.parse("This is a test <a componentName:id=\"a\" href=\"foo.html\"> <b componentName:id=\"b\">Bold!</b> "
						+ "<img componentName:id=\"img\" width=9 height=10 src=\"foo\"> <marker componentName:id=\"marker\"/> </a>");

		final MarkupStream markupStream = new MarkupStream(markup);

		final ComponentTag aOpen = (ComponentTag)markupStream.next();

		log.info(aOpen);
		Assert.assertTrue(aOpen.getName().equals("a"));
		Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

		markupStream.next();

		final ComponentTag boldOpen = (ComponentTag)markupStream.next();

		log.info(boldOpen);
		Assert.assertTrue(boldOpen.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.OPEN, boldOpen.getType());

		markupStream.next();

		final ComponentTag boldClose = (ComponentTag)markupStream.next();

		log.info(boldClose);
		Assert.assertTrue(boldClose.getName().equals("b"));
		Assert.assertEquals(XmlTag.Type.CLOSE, boldClose.getType());

		markupStream.next();

		final ComponentTag img = (ComponentTag)markupStream.next();

		log.info(img);
		Assert.assertTrue(img.getName().equals("img"));
		Assert.assertEquals(9, img.getAttributes().getInt("width"));
		Assert.assertEquals(10, img.getAttributes().getInt("height"));
		Assert.assertEquals(XmlTag.Type.OPEN, img.getType());

		markupStream.next();

		final ComponentTag marker = (ComponentTag)markupStream.next();

		log.info(marker);
		Assert.assertTrue(marker.getName().equals("marker"));
		Assert.assertEquals(XmlTag.Type.OPEN_CLOSE, marker.getType());

		markupStream.next();

		final ComponentTag aClose = (ComponentTag)markupStream.next();

		log.info(aClose);
		Assert.assertTrue(aClose.getName().equals("a"));

		Assert.assertNull(markupStream.next());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public final void test() throws Exception
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.setWicketNamespace("componentName");
		final IMarkup tokens = parser
				.parse("This is a test <a componentName:id=9> <b>bold</b> <b componentName:id=10/></a> of the emergency broadcasting system");

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
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.setWicketNamespace("componentName");
		final IMarkup tokens = parser.parse(docText);

		log.info("tok(0)=" + tokens.get(0));

		// without HtmlHeaderSectionHandler
		Assert.assertEquals(docText.substring(44), tokens.get(0).toString());
		// with HtmlHeaderSectionHandler
		// Assert.assertEquals(docText.substring(44, 147),
		// tokens.get(0).toString());
	}

	private MarkupResourceStream newMarkupResourceStream(final IResourceStreamLocator locator,
			final Class c, final String style, final Locale locale, final String extension)
	{
		IResourceStream resource = locator.locate(c, c.getName().replace('.', '/'), style, locale,
				extension);
		MarkupResourceStream res = new MarkupResourceStream(resource, null, null);
		return res;
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
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.setWicketNamespace("wcn");

		IResourceStreamLocator locator = new ClassLoaderResourceStreamLocator();

		MarkupResourceStream resource = newMarkupResourceStream(locator, this.getClass(), "1",
				null, "html");

		IMarkup tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "2", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "3", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "4", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		// File from jar (URL resource)
		resource = newMarkupResourceStream(locator, PageExpiredErrorPage.class, null, null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "5", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "6", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "7", null, "html");
		tokens = parser.readAndParse(resource);
		log.info("tok(0)=" + tokens.get(0));
		// Assert.assertEquals(docText, tokens.get(0).toString());

		resource = newMarkupResourceStream(locator, this.getClass(), "8", null, "html");
		tokens = parser.readAndParse(resource);
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

		final MarkupParser parser = new MarkupParser(new XmlPullParser());

		parser.parse("<span wicket:id=\"test\"/>");
		parser.parse("<span wicket:id=\"test\">Body</span>");
		parser.parse("This is a test <span wicket:id=\"test\"/>");
		parser.parse("This is a test <span wicket:id=\"test\">Body</span>");
		parser.parse("<a wicket:id=\"[autolink]\" href=\"test.html\">Home</a>");

		parser.parse("<wicket:body/>");
		parser.parse("<wicket:border/>");
		parser.parse("<wicket:panel/>");

		try
		{
			parser.parse("<wicket:remove/>");
			assertTrue("Should have thrown an exception", false);
		}
		catch (MarkupException ex)
		{
			// ignore
		}

		IMarkup markup = parser.parse("<wicket:remove>  </wicket:remove>");
		assertEquals(0, markup.size());

		markup = parser.parse("<wicket:remove> <span id=\"test\"/> </wicket:remove>");
		assertEquals(0, markup.size());

		markup = parser.parse("<div><wicket:remove> <span id=\"test\"/> </wicket:remove></div>");
		assertEquals(2, markup.size());
		assertEquals("<div>", ((RawMarkup)markup.get(0)).toString());
		assertEquals("</div>", ((RawMarkup)markup.get(1)).toString());

		try
		{
			parser.parse("<wicket:remove> <wicket:remove> </wicket:remove> </wicket:remove>");
			assertTrue(
					"Should have thrown an exception: remove regions must not contain wicket-components",
					false);
		}
		catch (MarkupException ex)
		{
			// ignore
		}

		parser
				.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\"/>");
		parser
				.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">    </wicket:component>");
		parser
				.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">  <span wicket:id=\"msg\">hello world!</span></wicket:component>");
		parser
				.parse("<wicket:panel><div id=\"definitionsContentBox\"><span wicket:id=\"contentPanel\"/></div></wicket:panel>");
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
		final MarkupParser parser = new MarkupParser(new XmlPullParser());
		parser.setWicketNamespace("wcn");

		IMarkup markup = parser.parse("<span wcn:id=\"test\"/>");
		assertEquals(1, markup.size());

		markup = parser.parse("<span wicket:id=\"test\"/>");
		assertEquals(1, markup.size());

		WicketTagIdentifier.registerWellKnownTagName("xxx");
		markup = parser.parse("<wcn:xxx>  </wcn:xxx>");
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
		final MarkupParser parser = new MarkupParser(new XmlPullParser());

		IMarkup markup = parser
				.parse("<html wicket:id=\"test\"><script language=\"JavaScript\">... <x a> ...</script></html>");
		assertEquals(3, markup.size());
		assertEquals("html", ((ComponentTag)markup.get(0)).getName());
		assertEquals("html", ((ComponentTag)markup.get(2)).getName());
		assertEquals(true, markup.get(1) instanceof RawMarkup);
		assertEquals("<script language=\"JavaScript\">... <x a> ...</script>", ((RawMarkup)markup
				.get(1)).toString());
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ResourceStreamNotFoundException
	 */
	public final void testBalancing() throws IOException, ResourceStreamNotFoundException
	{
		final MarkupParser parser = new MarkupParser(new XmlPullParser());

		// Note: <img> is one of these none-balanced HTML tags
		IMarkup markup = parser
				.parse("<span wicket:id=\"span\"><img wicket:id=\"img\"><span wicket:id=\"span2\"></span></span>");

		ComponentTag t = (ComponentTag)markup.get(0);
		assertEquals(t.getId(), "span");
		assertEquals(t.getPath(), null);

		t = (ComponentTag)markup.get(1);
		assertEquals(t.getId(), "img");
		assertEquals(t.getPath(), "span");

		t = (ComponentTag)markup.get(2);
		assertEquals(t.getId(), "span2");
		assertEquals(t.getPath(), "span");
	}
}
