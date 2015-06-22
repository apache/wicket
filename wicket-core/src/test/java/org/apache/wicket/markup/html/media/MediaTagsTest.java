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
package org.apache.wicket.markup.html.media;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

public class MediaTagsTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MediaComponentsApplication();
	}

	@Test
	public void audioTagIsRenderedRight()
	{
		tester.startPage(MediaTagsTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester createTagByAttribute = TagTester.createTagByAttribute(lastResponseAsString,
			"audio");
		assertTrue(createTagByAttribute.hasAttribute("autoplay"));
		assertTrue(createTagByAttribute.hasAttribute("controls"));
		assertTrue(createTagByAttribute.hasAttribute("loop"));
		assertTrue(createTagByAttribute.hasAttribute("muted"));
		assertEquals("user-credentials", createTagByAttribute.getAttribute("crossorigin"));
		String attribute = createTagByAttribute.getAttribute("src");
		assertTrue("The time period is set right in the src attribute",
			attribute.contains("#t=5,10"));
		assertTrue("page parameter is in the url of the src attribute",
			attribute.contains("test=test"));
	}

	@Test
	public void videoTagIsRenderedRight()
	{
		tester.startPage(MediaTagsTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester createTagByAttribute = TagTester.createTagByAttribute(lastResponseAsString,
			"video");
		String attribute = createTagByAttribute.getAttribute("poster");
		assertTrue("page parameter is in the url of the poster",
			attribute.contains("test2=test2"));
		String attributesrc = createTagByAttribute.getAttribute("src");
		assertTrue("video url is in the src attribute",
			attributesrc.contains("dummyVideo.m4a"));
		assertEquals("500", createTagByAttribute.getAttribute("width"));
		assertEquals("400", createTagByAttribute.getAttribute("height"));
	}

	@Test
	public void extendedVideoTagIsRenderedRight()
	{
		tester.startPage(MediaTagsExtendedTestPage.class);
		String lastResponseAsString = tester.getLastResponse().getDocument();
		TagTester createTagByAttribute = TagTester.createTagByAttribute(lastResponseAsString,
			"video");
		assertTrue(createTagByAttribute.hasChildTag("source"));
		assertTrue(createTagByAttribute.hasChildTag("track"));


		TagTester sourceTag = TagTester.createTagByAttribute(lastResponseAsString, "source");
		assertEquals("video/mp4", sourceTag.getAttribute("type"));
		assertEquals("screen and (device-width:500px)", sourceTag.getAttribute("media"));
		assertEquals("http://www.mytestpage.xc/video.m4a", sourceTag.getAttribute("src"));

		TagTester trackTag = TagTester.createTagByAttribute(lastResponseAsString, "track");

		assertTrue(trackTag.getAttribute("src").contains("dummySubtitles"));
		assertEquals("subtitles", trackTag.getAttribute("kind"));
		assertEquals("Subtitles of video", trackTag.getAttribute("label"));
		assertEquals("default", trackTag.getAttribute("default"));
		assertEquals("de", trackTag.getAttribute("srclang"));
	}
}
