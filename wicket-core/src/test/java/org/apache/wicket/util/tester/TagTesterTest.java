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
package org.apache.wicket.util.tester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test of TagTester
 */
class TagTesterTest
{
	/** Mock markup 1 */
	private static final String MARKUP_1 = "<p id=\"test\" class=\"class1\"><span class=\"class2\" id=\"test2\">mock</span></p>";

	private static final String AJAX_MARKUP_1 = "<?xml version='1.0' encoding='UTF-8'?>" +
			"<ajax-response><component id='comp1'><![CDATA[<div class='cls' id='compId'></div>]]></component></ajax-response>";

	// WICKET-5874
	private static final String NON_CLOSED_INPUT = "<p wicket:id=\"p\"><input wicket:id=\"wicketId\" type=\"text\"></p>";

	/**
	 * WICKET-6278
	 */
	@Test
	void tagNoRequiredClose() {
		TagTester tester = TagTester.createTagByAttribute(NON_CLOSED_INPUT, "wicket:id", "p");

		assertEquals("<input wicket:id=\"wicketId\" type=\"text\">", tester.getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5874
	 */
	@Test
	void getTagTesterForNonClosedTag()
	{
		TagTester tester = TagTester.createTagByAttribute(NON_CLOSED_INPUT, "wicket:id", "wicketId");
		assertNotNull(tester);

		String type = tester.getAttribute("type");
		assertEquals("text", type);

		assertNull(tester.getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-6172
	 */
	@Test
	void getTagTestersForNonClosedTag()
	{
		List<TagTester> testers = TagTester.createTagsByAttribute(NON_CLOSED_INPUT, "wicket:id", "wicketId", false);
		assertNotNull(testers);
		assertEquals(1, testers.size());

		String type = testers.get(0).getAttribute("type");
		assertEquals("text", type);

		assertNull(testers.get(0).getValue());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5137
	 */
	@Test
	void getTagInAjaxResponse()
	{
		TagTester tester = TagTester.createTagByAttribute(AJAX_MARKUP_1, "id", "compId");
		assertNotNull(tester);

		String cls = tester.getAttribute("class");
		assertEquals("cls", cls);
	}

	/**
	 * Test the static factory method
	 */
	@Test
	void createTagByAttribute()
	{
		TagTester tester = TagTester.createTagByAttribute(null, null, null);
		assertNull(tester);

		tester = TagTester.createTagByAttribute("<p id=\"test\">mock</p>", null, null);
		assertNull(tester);

		tester = TagTester.createTagByAttribute("<p id=\"test\">mock</p>", "id", null);
		assertNull(tester);

		tester = TagTester.createTagByAttribute("<p id=\"test\">mock</p>", "id", "test");
		assertNotNull(tester);
	}

	/**
	 * Test that getName returns the correct tag name.
	 */
	@Test
	void getName()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertEquals("p", tester.getName());


		tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertNotNull(tester);

		assertEquals("span", tester.getName());
	}

	/**
	 * Test that hasAttribute return true if the tag has the given attribute.
	 * 
	 * It also tests that the order of the attributes doesn't matter.
	 */
	@Test
	void hasAttribute()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertTrue(tester.hasAttribute("class"));


		tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertNotNull(tester);

		assertTrue(tester.hasAttribute("class"));
	}

	/**
	 * Get attribute should return the value of the attribute.
	 * 
	 * If the attribute doesn't exist on the tag, the method should return null.
	 */
	@Test
	void getAttribute()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertEquals("class1", tester.getAttribute("class"));

		// Nested
		tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertNotNull(tester);

		assertEquals("class2", tester.getAttribute("class"));

		// Case insensitive
		tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertNotNull(tester);

		assertEquals("class2", tester.getAttribute("CLASS"));

		// Return null if no attribute
		tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertNull(tester.getAttribute("noattribute"));

		// Test that an empty attribute is returned as an empty string
		tester = TagTester.createTagByAttribute("<p id=\"test\" empty=\"\">Mock</p>", "id", "test");
		assertNotNull(tester);

		assertEquals("", tester.getAttribute("empty"));
	}

	/**
	 * getAttributeContains should only return true if the attribute value contains the expected
	 * value. It should not be case in-sensitive and not trim the attribute value.
	 */
	@Test
	void getAttributeContains()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertTrue(tester.getAttributeContains("class", "ss1"));
		assertTrue(tester.getAttributeContains("class", "clas"));
		assertTrue(tester.getAttributeContains("class", "s"));
		assertTrue(tester.getAttributeContains("class", "1"));
		assertTrue(tester.getAttributeContains("CLASS", "1"));
		assertFalse(tester.getAttributeContains("class", "classs"));
		assertFalse(tester.getAttributeContains("class", "CLASS"));
		assertFalse(tester.getAttributeContains("class", "cLass1"));
		assertFalse(tester.getAttributeContains("class", "class1 "));
		assertFalse(tester.getAttributeContains("class", " class1"));
	}

	/**
	 * Test the convenience method getAttributeIs, which returns true if the attributes value is
	 * exactly the same as the parameter.
	 */
	@Test
	void getAttributeIs()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertTrue(tester.getAttributeIs("class", "class1"));
		assertFalse(tester.getAttributeIs("class", "class1 "));
		assertFalse(tester.getAttributeIs("class", " class1"));
		assertFalse(tester.getAttributeIs("class", "Class1"));

		assertTrue(tester.getAttributeIs("noattribute", null));
		assertFalse(tester.getAttributeIs("noattribute", "somevalue"));
		assertFalse(tester.getAttributeIs("class", null));
	}

	/**
	 * getAttributeEndsWith behaves the same as getAttributeContains, but the parameter which should
	 * be contained must only be at the end.
	 */
	@Test
	void getAttributeEndsWith()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertNotNull(tester);

		assertTrue(tester.getAttributeEndsWith("class", "1"));
		assertTrue(tester.getAttributeEndsWith("class", ""));
		assertTrue(tester.getAttributeEndsWith("class", "ss1"));
		assertTrue(tester.getAttributeEndsWith("class", "lass1"));

		assertFalse(tester.getAttributeEndsWith("class", "1 "));
		assertFalse(tester.getAttributeEndsWith("class", " "));
		assertFalse(tester.getAttributeEndsWith("class", "class 1"));
		assertFalse(tester.getAttributeEndsWith("class", "SS1"));
	}

	/**
	 * 
	 */
	@Test
	void hasChildTag()
	{
		TagTester tester = TagTester.createTagByAttribute(MARKUP_1, "id", "test");

		assertTrue(tester.hasChildTag("span"));
		assertTrue(tester.hasChildTag("SPAN"));

		assertFalse(tester.hasChildTag("span "));
		assertFalse(tester.hasChildTag("p"));

		try
		{
			tester.hasChildTag("");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		try
		{
			tester.hasChildTag(null);
			fail();
		}
		catch (IllegalArgumentException e)
		{
			// expected
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}


		tester = TagTester.createTagByAttribute("<p id=\"test\">mock</p>", "id", "test");

		assertFalse(tester.hasChildTag("span"));
		assertFalse(tester.hasChildTag("p"));
	}

	@Test
	void getChildByTagName()
	{
		TagTester tester = TagTester.createTagByAttribute(
			"<div id=\"id\">" +
				"<div class=\"radio\">" +
					"<label>" +
						"<input name=\"id\" type=\"radio\" value=\"0\" id=\"id1-0\"/> One" +
					"</label>" +
				"</div>" +
			"</div>", "id", "id");
		assertNotNull(tester.getChild("DIV")); // case-insensitive
		TagTester divClassRadioTagTester = tester.getChild("div");
		assertNotNull(divClassRadioTagTester);
		TagTester labelTagTester = divClassRadioTagTester.getChild("label");
		String labelMarkup = labelTagTester.getValue();
		assertThat(labelMarkup).endsWith(" One");
	}

	/**
	 * Test getMarkup returns the open-tag + content + close-tag
	 */
	@Test
	void getMarkup()
	{
		TagTester tagTester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");

		assertEquals("<span class=\"class2\" id=\"test2\">mock</span>", tagTester.getMarkup());
	}

	/**
	 * Test getValue returns the data between the open and close tag.
	 */
	@Test
	void getValue()
	{
		TagTester tagTester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertEquals("mock", tagTester.getValue());

		// Check that getValue also returns tags if the content of a tag is containing tags
		TagTester tagTester2 = TagTester.createTagByAttribute(MARKUP_1, "id", "test");
		assertEquals(tagTester.getMarkup(), tagTester2.getValue());
	}
	
	/**
	 * https://issues.apache.org/jira/browse/WICKET-6173
	 */
	@Test
	void valueFromTagsByAttribute()
	{
		TagTester tagTester = TagTester.createTagByAttribute(MARKUP_1, "id", "test2");
		assertEquals("mock", tagTester.getValue());
	}
	
    private static final String MARKUP =
        "<wicket:panel>" +
            "<ul wicket:id=\"container\">" +
                "<wicket:container wicket:id=\"items\">" +
                    "<li wicket:id=\"item\" id=\"item1\">" +
                        "<p wicket:id=\"p\" id=\"p1\">" +
                            "<img wicket:id=\"img\" src=\"bild1.jpg\">" +
                        "</p>" +
                        "<hr wicket:id=\"hr\" id=\"hr1\"/>" +
                    "</li>" +
                    "<li wicket:id=\"item\" id=\"item2\">" +
                        "<p wicket:id=\"p\" id=\"p2\">" +
                        "<img wicket:id=\"img\" src=\"bild2.jpg\">" +
                        "<hr wicket:id=\"hr\" id=\"hr2\"/>" +
                    "</li>" +
                "</wicket:container>" +
            "</ul>" +
        "</wicket:panel>";

    private static final String WRONG_MARKUP =
        "<wicket:panel>" +
            "<ul wicket:id=\"container\">" +
                "<wicket:container wicket:id=\"items\">" +
                    "<li wicket:id=\"item\" id=\"item1\">" +
                        "<span wicket:id=\"p\" id=\"p1\">" +
                        "<img wicket:id=\"img\" src=\"bild1.jpg\">" +
                        "<hr wicket:id=\"hr\" id=\"hr1\"/>" +
                    "</li>" +
                    "<li wicket:id=\"item\" id=\"item2\">" +
                        "<span wicket:id=\"p\" id=\"p2\">" +
                            "<img wicket:id=\"img\" src=\"bild2.jpg\">" +
                            "<hr wicket:id=\"hr\" id=\"hr2\"/>" +
                        "</span>" +
                    "</li>" +
                "</wicket:container>" +
            "</ul>" +
        "</wicket:panel>";

    /**
     * WICKET-6220
     */
    @Test
	void testOpenAndClose() {
        List<TagTester> tags = TagTester.createTagsByAttribute(MARKUP, "wicket:id", "item", false);
        assertEquals(2, tags.size());
        assertEquals("li", tags.get(0).getName());
        assertEquals("item1", tags.get(0).getAttribute("id"));
        assertEquals("<p wicket:id=\"p\" id=\"p1\"><img wicket:id=\"img\" src=\"bild1.jpg\"></p><hr wicket:id=\"hr\" id=\"hr1\"/>", tags.get(0).getValue());
        assertEquals("li", tags.get(1).getName());
        assertEquals("item2", tags.get(1).getAttribute("id"));
        assertEquals("<p wicket:id=\"p\" id=\"p2\"><img wicket:id=\"img\" src=\"bild2.jpg\"><hr wicket:id=\"hr\" id=\"hr2\"/>", tags.get(1).getValue());
    }

    /**
     * WICKET-6220
     */
    @Test
	void testWrongHtmlStructure() {
        List<TagTester> tags = TagTester.createTagsByAttribute(WRONG_MARKUP, "wicket:id", "p", false);
        assertEquals(1, tags.size());
        assertEquals("span", tags.get(0).getName());
        assertEquals("p2", tags.get(0).getAttribute("id"));
        assertEquals("<img wicket:id=\"img\" src=\"bild2.jpg\"><hr wicket:id=\"hr\" id=\"hr2\"/>", tags.get(0).getValue());
    }

    /**
     * WICKET-6220
     */
    @Test
	void testOpenOrClose() {
        List<TagTester> tags = TagTester.createTagsByAttribute(MARKUP, "wicket:id", "p", false);
        assertEquals(2, tags.size());
        assertEquals("p", tags.get(0).getName());
        assertEquals("p1", tags.get(0).getAttribute("id"));
        assertEquals("<img wicket:id=\"img\" src=\"bild1.jpg\">", tags.get(0).getValue());
        assertEquals("p", tags.get(1).getName());
        assertEquals("p2", tags.get(1).getAttribute("id"));
        assertEquals("<p wicket:id=\"p\" id=\"p2\">", tags.get(1).getMarkup());
    }

    /**
     * WICKET-6220
     */
    @Test
	void testOpen() {
        List<TagTester> tags = TagTester.createTagsByAttribute(MARKUP, "wicket:id", "img", false);
        assertEquals(2, tags.size());
        assertEquals("img", tags.get(0).getName());
        assertEquals("bild1.jpg", tags.get(0).getAttribute("src"));
        assertEquals("<img wicket:id=\"img\" src=\"bild1.jpg\">", tags.get(0).getMarkup());
        assertEquals("img", tags.get(1).getName());
        assertEquals("bild2.jpg", tags.get(1).getAttribute("src"));
        assertEquals("<img wicket:id=\"img\" src=\"bild2.jpg\">", tags.get(1).getMarkup());
    }

    /**
     * WICKET-6220
     */
    @Test
	void testOpenClose() {
        List<TagTester> tags = TagTester.createTagsByAttribute(MARKUP, "wicket:id", "hr", false);
        assertEquals(2, tags.size());
        assertEquals("hr", tags.get(0).getName());
        assertEquals("hr1", tags.get(0).getAttribute("id"));
        assertEquals("<hr wicket:id=\"hr\" id=\"hr1\"/>", tags.get(0).getMarkup());
        assertEquals("hr", tags.get(1).getName());
        assertEquals("hr2", tags.get(1).getAttribute("id"));
        assertEquals("<hr wicket:id=\"hr\" id=\"hr2\"/>", tags.get(1).getMarkup());
    }	
}