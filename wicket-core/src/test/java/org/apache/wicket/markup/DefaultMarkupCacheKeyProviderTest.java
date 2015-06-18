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
package org.apache.wicket.markup;

import java.util.Locale;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link DefaultMarkupCacheKeyProvider}.
 */
public class DefaultMarkupCacheKeyProviderTest extends WicketTestCase
{

	/**
       */
	@Test
	public void localeLanguageCountryVariant()
	{
		DefaultMarkupCacheKeyProvider provider = new DefaultMarkupCacheKeyProvider();

		Foo foo = new Foo("foo");
		assertEquals("org.apache.wicket.markup.Foo.html", provider.getCacheKey(foo, foo.getClass()));

		foo.locale = new Locale("");
		assertEquals("org.apache.wicket.markup.Foo_.html",
			provider.getCacheKey(foo, foo.getClass()));

		foo.locale = new Locale("language");
		assertEquals("org.apache.wicket.markup.Foo_language.html", provider.getCacheKey(foo,
			foo.getClass()));

		foo.locale = new Locale("", "COUNTRY");
		assertEquals("org.apache.wicket.markup.Foo__COUNTRY.html", provider.getCacheKey(foo,
			foo.getClass()));

		// variant only is ignored
		foo.locale = new Locale("", "", "variant");
		assertEquals("org.apache.wicket.markup.Foo_.html",
			provider.getCacheKey(foo, foo.getClass()));

		foo.locale = new Locale("language", "COUNTRY");
		assertEquals("org.apache.wicket.markup.Foo_language_COUNTRY.html", provider.getCacheKey(
			foo, foo.getClass()));

		foo.locale = new Locale("language", "", "variant");
		assertEquals("org.apache.wicket.markup.Foo_language__variant.html", provider.getCacheKey(
			foo, foo.getClass()));

		foo.locale = new Locale("", "COUNTRY", "variant");
		assertEquals("org.apache.wicket.markup.Foo__COUNTRY_variant.html", provider.getCacheKey(
			foo, foo.getClass()));

		foo.locale = new Locale("language", "COUNTRY", "variant");
		assertEquals("org.apache.wicket.markup.Foo_language_COUNTRY_variant.html",
			provider.getCacheKey(foo, foo.getClass()));
	}
}

class Foo extends WebMarkupContainer
{

	private static final long serialVersionUID = 1L;

	public Locale locale;

	public Foo(String id)
	{
		super(id);
	}

	@Override
	protected void onRender()
	{
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	public MarkupType getMarkupType()
	{
		return MarkupType.HTML_MARKUP_TYPE;
	}
}