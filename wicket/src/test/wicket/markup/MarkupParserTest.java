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

import wicket.markup.ComponentTag;
import wicket.markup.Markup;
import wicket.markup.MarkupParser;
import wicket.markup.MarkupStream;
import wicket.markup.html.PageExpiredErrorPage;
import wicket.util.resource.Resource;
import wicket.util.resource.ResourceNotFoundException;
import wicket.util.string.StringValueConversionException;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Test cases for markup parser.
 * @author Jonathan Locke
 */
public final class MarkupParserTest extends TestCase
{
    /**
     * 
     * @throws StringValueConversionException
     * @throws ParseException
     */
    public final void testTagParsing() throws StringValueConversionException, ParseException
    {
        final MarkupParser parser = new MarkupParser("componentName", "wicket");
        final Markup markup = parser
                .parse("This is a test <a componentName=\"a\" href=\"foo.html\"> <b componentName=\"b\">Bold!</b> "
                        + "<img componentName=\"img\" width=9 height=10 src=\"foo\"> <marker componentName=\"marker\"/> </a>");

        final MarkupStream markupStream = new MarkupStream(markup);

        final ComponentTag aOpen = (ComponentTag) markupStream.next();

        System.out.println(aOpen);
        Assert.assertTrue(aOpen.getName().equals("a"));
        Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

        markupStream.next();

        final ComponentTag boldOpen = (ComponentTag) markupStream.next();

        System.out.println(boldOpen);
        Assert.assertTrue(boldOpen.getName().equals("b"));
        Assert.assertEquals(ComponentTag.OPEN, boldOpen.getType());

        markupStream.next();

        final ComponentTag boldClose = (ComponentTag) markupStream.next();

        System.out.println(boldClose);
        Assert.assertTrue(boldClose.getName().equals("b"));
        Assert.assertEquals(ComponentTag.CLOSE, boldClose.getType());

        markupStream.next();

        final ComponentTag img = (ComponentTag) markupStream.next();

        System.out.println(img);
        Assert.assertTrue(img.getName().equals("img"));
        Assert.assertEquals(9, img.getAttributes().getInt("width"));
        Assert.assertEquals(10, img.getAttributes().getInt("height"));
        Assert.assertEquals(ComponentTag.OPEN, img.getType());

        markupStream.next();

        final ComponentTag marker = (ComponentTag) markupStream.next();

        System.out.println(marker);
        Assert.assertTrue(marker.getName().equals("marker"));
        Assert.assertEquals(ComponentTag.OPEN_CLOSE, marker.getType());

        markupStream.next();

        final ComponentTag aClose = (ComponentTag) markupStream.next();

        System.out.println(aClose);
        Assert.assertTrue(aClose.getName().equals("a"));

        Assert.assertNull(markupStream.next());
    }

    /**
     * 
     * @throws StringValueConversionException
     * @throws ParseException
     */
    public final void test() throws StringValueConversionException, ParseException
    {
        final MarkupParser parser = new MarkupParser("componentName", "wicket");
        final Markup tokens = parser
                .parse("This is a test <a componentName=9> <b>bold</b> <b componentName=10/></a> of the emergency broadcasting system");

        System.out.println("tok(0)=" + tokens.get(0));
        System.out.println("tok(1)=" + tokens.get(1));
        System.out.println("tok(2)=" + tokens.get(2));
        System.out.println("tok(3)=" + tokens.get(3));
        System.out.println("tok(4)=" + tokens.get(4));
        System.out.println("tok(5)=" + tokens.get(5));

        Assert.assertTrue(tokens.get(0).equals("This is a test "));

        final ComponentTag a = (ComponentTag) tokens.get(1);

        Assert.assertEquals(9, a.getAttributes().getInt("componentName"));
        Assert.assertTrue(tokens.get(2).equals(" <b>bold</b> "));

        final ComponentTag b = (ComponentTag) tokens.get(3);

        Assert.assertEquals(10, b.getAttributes().getInt("componentName"));

        final ComponentTag closeA = (ComponentTag) tokens.get(4);

        Assert.assertEquals("a", closeA.getName());
        Assert.assertTrue(tokens.get(5).equals(" of the emergency broadcasting system"));
    }

    /**
     * 
     * @throws ParseException
     */
    public final void testXhtmlDocument() throws ParseException {
        final String docText = "" +
           "<?xml version='1.0' encoding='iso-8859-1' ?>" +
           "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
           "<html>" +
           "<head><title>Some Page</title></head>" +
           "<body><h1>XHTML Test</h1></body>" +
           "</html>";
        final MarkupParser parser = new MarkupParser("componentName", "wicket");
        final Markup tokens = parser.parse(docText);

        System.out.println("tok(0)=" + tokens.get(0));
        Assert.assertEquals(docText, tokens.get(0).toString());
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
        final MarkupParser parser = new MarkupParser("wcn", "wicket");
        Resource resource = Resource.locate(null, this.getClass(), "1", null, "html");
        Markup tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = Resource.locate(null, this.getClass(), "2", null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = Resource.locate(null, this.getClass(), "3", null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = Resource.locate(null, this.getClass(), "4", null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        // File from jar (URL resource)
        resource = Resource.locate(null, PageExpiredErrorPage.class, null, null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = Resource.locate(null, this.getClass(), "5", null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
        //Assert.assertEquals(docText, tokens.get(0).toString());

        resource = Resource.locate(null, this.getClass(), "6", null, "html");
        tokens = parser.read(resource);
        System.out.println("tok(0)=" + tokens.get(0));
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
	    final MarkupParser parser = new MarkupParser("wicket", "wicket");
	    
	    parser.parse("<span wicket=\"test\"/>");

	    parser.parse("<span wicket=\"test\">Body</span>");
	    
	    parser.parse("This is a test <span wicket=\"test\"/>");

	    parser.parse("This is a test <span wicket=\"test\">Body</span>");
	    
	    parser.parse("<span id=\"wicket-test\"/>");

	    parser.parse("<span id=\"wicket-test\">Body</span>");
	    
	    parser.parse("<a wicket=\"[autolink]\" href=\"test.html\">Home</a>");
	    
	    parser.parse("<span id=\"wicket-test\"/><wicket:param key=value/>");
	    
	    parser.parse("<span id=\"wicket-test\"/><wicket:param key=\"value\" />");
	    
	    try
	    {
	        parser.parse("<span id=\"wicket-test\"/>whatever<wicket:param key=\"value\" />");
	        assertTrue("Should have thrown an exception", false);
	    }
	    catch (ParseException ex)
	    {
	        ; // ignore
	    }
	    
	    parser.parse("<span id=\"wicket-test\"/><wicket:param key=\"value\" /><wicket:param key2=\"value2\" />");
	    
	    parser.parse("<span id=\"wicket-test\"/>   <wicket:param key=\"value\" />   <wicket:param key2=\"value2\" />");
	    
	    parser.parse("<span id=\"wicket-test\"/> \n\r   <wicket:param key=\"value\" />\n\r\t   <wicket:param key2=\"value2\" />");
	    
	    //parser.parse("<span id=\"wicket-test\"/><wicket:param name=myParam>value</wicket>");
	    
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

	    parser.parse("<wicket:remove>  </wicket:remove>");

	    parser.parse("<wicket:remove> <span id=\"test\"/> </wicket:remove>");

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
	    
	    parser.parse("<wicket:panel><div id=\"definitionsContentBox\"><span id=\"wicket-contentPanel\"/></div></wicket:panel>");
   	}
}

///////////////////////////////// End of File /////////////////////////////////
