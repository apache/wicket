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
package com.voicetribe.util.parse.test;

import com.voicetribe.util.parse.Tag;
import com.voicetribe.util.parse.TagParser;
import com.voicetribe.util.string.StringValueConversionException;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.text.ParseException;

/**
 * Test cases for this object
 * @author Jonathan Locke
 */
public final class TagParserTest extends TestCase
{
    public void test() throws StringValueConversionException, ParseException
    {
        final TagParser parser = new TagParser();

        parser.setInput("This is a test <a href=\"foo.html\"> <b>Bold!</b> "
                + "<img width=9 height=10 src=\"foo\"> <marker/> </a>");

        final Tag aOpen = parser.nextTag();

        System.out.println(aOpen);
        Assert.assertTrue(aOpen.getName().equals("a"));
        Assert.assertEquals("foo.html", aOpen.getAttributes().getString("href"));

        final Tag boldOpen = parser.nextTag();

        System.out.println(boldOpen);
        Assert.assertTrue(boldOpen.getName().equals("b"));
        Assert.assertTrue(boldOpen.isOpen());
        Assert.assertTrue(!boldOpen.isClose());

        final Tag boldClose = parser.nextTag();

        System.out.println(boldClose);
        Assert.assertTrue(boldClose.getName().equals("b"));
        Assert.assertTrue(!boldClose.isOpen());
        Assert.assertTrue(boldClose.isClose());

        final Tag img = parser.nextTag();

        System.out.println(img);
        Assert.assertTrue(img.getName().equals("img"));
        Assert.assertEquals(9, img.getAttributes().getInt("width"));
        Assert.assertEquals(10, img.getAttributes().getInt("height"));
        Assert.assertTrue(img.isOpen());
        Assert.assertTrue(!img.isClose());

        final Tag marker = parser.nextTag();

        System.out.println(marker);
        Assert.assertTrue(marker.getName().equals("marker"));
        Assert.assertTrue(marker.isOpen());
        Assert.assertTrue(marker.isClose());

        final Tag aClose = parser.nextTag();

        System.out.println(aClose);
        Assert.assertTrue(aClose.getName().equals("a"));

        Assert.assertNull(parser.nextTag());
    }
}

///////////////////////////////// End of File /////////////////////////////////
