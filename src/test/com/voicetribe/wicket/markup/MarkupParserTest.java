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
package com.voicetribe.wicket.markup;

import com.voicetribe.util.string.StringValueConversionException;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;

/**
 * Test cases for markup parser.
 * @author Jonathan Locke
 */
public final class MarkupParserTest extends TestCase
{
    public final void testTagParsing() throws StringValueConversionException, ParseException
    {
        final MarkupParser parser = new MarkupParser("componentName", 0);
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

    public final void test() throws StringValueConversionException, ParseException
    {
        final MarkupParser parser = new MarkupParser("componentName", 0);
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

    public final void testXhtmlDocument() throws ParseException {
        final String docText = "" +
           "<?xml version='1.0' encoding='iso-8859-1' ?>" +
           "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
           "<html>" +
           "<head><title>Some Page</title></head>" +
           "<body><h1>XHTML Test</h1></body>" +
           "</html>";
        final MarkupParser parser = new MarkupParser("componentName", 0);
        final Markup tokens = parser.parse(docText);

        System.out.println("tok(0)=" + tokens.get(0));
        Assert.assertEquals(docText, tokens.get(0).toString());
    }
}

///////////////////////////////// End of File /////////////////////////////////
