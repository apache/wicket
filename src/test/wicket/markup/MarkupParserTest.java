/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.markup.html.pages.PageExpiredErrorPage;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.XmlTag;
import wicket.protocol.http.MockWebApplication;
import wicket.util.resource.IResource;
import wicket.util.resource.ResourceNotFoundException;
import wicket.util.resource.locator.ClassLoaderResourceLocator;
import wicket.util.resource.locator.ResourceLocator;
import wicket.util.string.StringValueConversionException;


/**
 * Test cases for markup parser.
 * @author Jonathan Locke
 */
public final class MarkupParserTest extends TestCase
{
	private static Log log = LogFactory.getLog(MarkupParserTest.class);

    /**
     * 
     * @throws StringValueConversionException
     * @throws Exception
     */
    public final void testTagParsing() throws Exception
    {
        final MarkupParser parser = new MarkupParser(new XmlPullParser(), "componentName");
        final Markup markup = parser.parse(
                "This is a test <a componentName:id=\"a\" href=\"foo.html\"> <b componentName:id=\"b\">Bold!</b> "
                + "<img componentName:id=\"img\" width=9 height=10 src=\"foo\"> <marker componentName:id=\"marker\"/> </a>");

        final MarkupStream markupStream = new MarkupStream(markup);

        final ComponentTag aOpen = (ComponentTag) markupStream.next();

        log.info(aOpen);
        Assert.assertTrue(aOpen.getName().equals("a"));
        Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

        markupStream.next();

        final ComponentTag boldOpen = (ComponentTag) markupStream.next();

        log.info(boldOpen);
        Assert.assertTrue(boldOpen.getName().equals("b"));
        Assert.assertEquals(XmlTag.OPEN, boldOpen.getType());

        markupStream.next();

        final ComponentTag boldClose = (ComponentTag) markupStream.next();

        log.info(boldClose);
        Assert.assertTrue(boldClose.getName().equals("b"));
        Assert.assertEquals(XmlTag.CLOSE, boldClose.getType());

        markupStream.next();

        final ComponentTag img = (ComponentTag) markupStream.next();

        log.info(img);
        Assert.assertTrue(img.getName().equals("img"));
        Assert.assertEquals(9, img.getAttributes().getInt("width"));
        Assert.assertEquals(10, img.getAttributes().getInt("height"));
        Assert.assertEquals(XmlTag.OPEN, img.getType());

        markupStream.next();

        final ComponentTag marker = (ComponentTag) markupStream.next();

        log.info(marker);
        Assert.assertTrue(marker.getName().equals("marker"));
        Assert.assertEquals(XmlTag.OPEN_CLOSE, marker.getType());

        markupStream.next();

        final ComponentTag aClose = (ComponentTag) markupStream.next();

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
        final MarkupParser parser = new MarkupParser(new XmlPullParser(), "componentName");
        final Markup tokens = parser
                .parse("This is a test <a componentName:id=9> <b>bold</b> <b componentName:id=10/></a> of the emergency broadcasting system");

        log.info("tok(0)=" + tokens.get(0));
        log.info("tok(1)=" + tokens.get(1));
        log.info("tok(2)=" + tokens.get(2));
        log.info("tok(3)=" + tokens.get(3));
        log.info("tok(4)=" + tokens.get(4));
        log.info("tok(5)=" + tokens.get(5));

        Assert.assertTrue(tokens.get(0).equals("This is a test "));

        final ComponentTag a = (ComponentTag) tokens.get(1);

        Assert.assertEquals(9, a.getAttributes().getInt("componentName:id"));
        Assert.assertTrue(tokens.get(2).equals(" <b>bold</b> "));

        final ComponentTag b = (ComponentTag) tokens.get(3);

        Assert.assertEquals(10, b.getAttributes().getInt("componentName:id"));

        final ComponentTag closeA = (ComponentTag) tokens.get(4);

        Assert.assertEquals("a", closeA.getName());
        Assert.assertTrue(tokens.get(5).equals(" of the emergency broadcasting system"));
    }

    /**
     * 
     * @throws Exception
     */
    public final void testXhtmlDocument() throws Exception {
        final String docText = "" +
           "<?xml version='1.0' encoding='iso-8859-1' ?>" +
           "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
           "<html>" +
           "<head><title>Some Page</title></head>" +
           "<body><h1>XHTML Test</h1></body>" +
           "</html>";
        final MarkupParser parser = new MarkupParser(new XmlPullParser(), "componentName");
        final Markup tokens = parser.parse(docText);

        log.info("tok(0)=" + tokens.get(0));
        Assert.assertEquals(docText.substring(44), tokens.get(0).toString());
    }

    /**
     * 
     * @throws ParseException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public final void testFileDocument() throws ParseException,
            ResourceNotFoundException, IOException
    {
        final MarkupParser parser = new MarkupParser(new XmlPullParser(), "wcn");
        
        ResourceLocator locator = new ResourceLocator(new ClassLoaderResourceLocator());
        
        IResource resource = locator.locate(this.getClass(), "1", null, "html");
        Markup tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = locator.locate(this.getClass(), "2", null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = locator.locate(this.getClass(), "3", null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = locator.locate(this.getClass(), "4", null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        // File from jar (URL resource)
        resource = locator.locate(PageExpiredErrorPage.class, null, null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = locator.locate(this.getClass(), "5", null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = locator.locate(this.getClass(), "6", null, "html");
        tokens = parser.readAndParse(resource);
        log.info("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());
    }

    /**
     * Test &lt;wicket: .
     * @throws ParseException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public final void testWicketTag() throws ParseException,
    	ResourceNotFoundException, IOException
   	{
	    final MarkupParser parser = new MarkupParser(new XmlPullParser(), "wicket");
	    
	    parser.parse("<span wicket:id=\"test\"/>");

	    parser.parse("<span wicket:id=\"test\">Body</span>");
	    
	    parser.parse("This is a test <span wicket:id=\"test\"/>");

	    parser.parse("This is a test <span wicket:id=\"test\">Body</span>");
	    
	    parser.parse("<a wicket:id=\"[autolink]\" href=\"test.html\">Home</a>");
	    
	    parser.parse("<span wicket:id=\"test\"/><wicket:param key=value/>");
	    
	    parser.parse("<span wicket:id=\"test\"/><wicket:param key=\"value\" />");
	    
	    try
	    {
	        parser.parse("<span wicket:id=\"test\"/>whatever<wicket:param key=\"value\" />");
	        assertTrue("Should have thrown an exception", false);
	    }
	    catch (MarkupException ex)
	    {
	        ; // ignore
	    }
	    
	    parser.parse("<span wicket:id=\"test\"/><wicket:param key=\"value\" /><wicket:param key2=\"value2\" />");
	    
	    parser.parse("<span wicket:id=\"test\"/>   <wicket:param key=\"value\" />   <wicket:param key2=\"value2\" />");
	    
	    parser.parse("<span wicket:id=\"test\"/> \n\r   <wicket:param key=\"value\" />\n\r\t   <wicket:param key2=\"value2\" />");
	    
	    //parser.parse("<span wicket:id=\"test\"/><wicket:param name=myParam>value</wicket>", null);
	    
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
	        ; // ignore
	    }

	    Markup markup = parser.parse("<wicket:remove>  </wicket:remove>");
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
	        assertTrue("Should have thrown an exception: remove regions must not contain wicket-components", false);
	    }
	    catch (MarkupException ex)
	    {
	        ; // ignore
	    }
	    
	    parser.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\"/>");
	    parser.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">    </wicket:component>");
	    parser.parse("<wicket:component name = \"componentName\" class = \"classname\" param1 = \"value1\">  <span wicket:id=\"msg\">hello world!</span></wicket:component>");
	    
	    parser.parse("<wicket:panel><div id=\"definitionsContentBox\"><span wicket:id=\"contentPanel\"/></div></wicket:panel>");
   	}

    /**
     * Test &lt;wicket: .
     * @throws ParseException
     * @throws ResourceNotFoundException
     * @throws IOException
     */
    public final void testDefaultWicketTag() throws ParseException,
    	ResourceNotFoundException, IOException
   	{
	    final MarkupParser parser = new MarkupParser(new XmlPullParser(), "wcn");
	    
	    Markup markup = parser.parse("<span wcn:id=\"test\"/>");
	    assertEquals(1, markup.size());

	    markup = parser.parse("<span wicket:id=\"test\"/>");
	    assertEquals(1, markup.size());
	    
	    markup = parser.parse("<wcn:xxx>  </wcn:xxx>");
	    assertEquals(3, markup.size());

	    final ApplicationSettings settings = new ApplicationSettings(new MockWebApplication(null));
	    settings.setWicketNamespace("wcn");
	    parser.configure(settings);
	    markup = parser.parse("<wicket:xxx>  </wicket:xxx>");
	    assertEquals(1, markup.size());
   	}
}
