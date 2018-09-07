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
package org.apache.wicket.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.image.ImageTest;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.request.resource.caching.ResourceUrl;
import org.apache.wicket.request.resource.caching.version.MessageDigestResourceVersion;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CssUrlReplacerTest extends WicketTestCase
{

	private static final String DECORATION_SUFFIX = "--decorated";

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getResourceSettings().setCachingStrategy(
					new FilenameWithVersionResourceCachingStrategy("=VER=",
						new MessageDigestResourceVersion())
					{
						@Override
						public void decorateUrl(ResourceUrl url, IStaticCacheableResource resource)
						{
							url.setFileName(url.getFileName() + DECORATION_SUFFIX);
						}
					});
			}
			
			@Override
			public Session newSession(Request request, Response response) {
				Session newSession = super.newSession(request, response);
				newSession.setLocale(Locale.ROOT);
				return newSession;
			}
			
		};
	}

	@BeforeEach
	void before()
	{
		tester.getSession().setLocale(Locale.ENGLISH);
	}

	@Test
	void doNotProcessFullUrls()
	{
		String input = ".class {background-image: url('http://example.com/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertEquals(input, processed);
	}

	@Test
	void doNotProcessDataUrls_WICKET_6290()
	{
		String input = ".class {background-image: url(data:image/gif;base64,R0lGODlhEAAQAMQAAORHH);}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertEquals(input, processed);
	}

	@Test
	void doNotProcessContextAbsoluteUrls()
	{
		String input = ".class {background-image: url('/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertEquals(input, processed);
	}

	@Test
	void sameFolderSingleQuotes()
	{
		String input = ".class {background-image: url('some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(
			processed,
			is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/some.img" +
				DECORATION_SUFFIX + "');}"));
	}

	@Test
	void sameFolderDoubleQuotes()
	{
		String input = ".class {background-image: url(\"some.img\");}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(
			processed,
			is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/some.img" +
				DECORATION_SUFFIX + "');}"));
	}

	@Test
	void parentFolderAppendFolder()
	{
		String input = ".class {background-image: url('../images/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(
			processed,
			is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/images/some.img" +
				DECORATION_SUFFIX + "');}"));
	}

	@Test
	void sameFolderAppendFolder()
	{
		String input = ".class {background-image: url('./images/some.img');}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(
			processed,
			is(".class {background-image: url('./wicket/resource/org.apache.wicket.resource.CssUrlReplacerTest/res/css/images/some.img" +
				DECORATION_SUFFIX + "');}"));
	}

	@Test
	void base64EncodedImage()
	{
		String input = ".class {background-image: url('Beer.gif?embedBase64');}";
		Class<?> scope = ImageTest.class;
		String cssRelativePath = "some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();
		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(
			processed,
			containsString(".class {background-image: url(data:image/gif;base64,R0lGODlh1wATAXAAACH5BAEAAP8ALAAAAADXA"));
	}

	@Test
	void severalUrls()
	{
		String input = ".class {\n" + "a: url('../images/a.img');\n" + "b: url('./b.img');\n" + "}";
		Class<?> scope = CssUrlReplacerTest.class;
		String cssRelativePath = "res/css/some.css";
		CssUrlReplacer replacer = new CssUrlReplacer();

		String processed = replacer.process(input, scope, cssRelativePath);
		assertThat(processed, containsString("CssUrlReplacerTest/res/images/a.img" +
			DECORATION_SUFFIX + "');"));
		assertThat(processed, containsString("CssUrlReplacerTest/res/css/b.img" +
			DECORATION_SUFFIX + "');"));
	}
}
