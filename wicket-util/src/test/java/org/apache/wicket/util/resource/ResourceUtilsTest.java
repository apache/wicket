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
package org.apache.wicket.util.resource;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** */
public class ResourceUtilsTest
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5706
	 */
	@Test
	public void getLocaleFromFilename()
	{
		ResourceUtils.PathLocale pathLocale;

		pathLocale = ResourceUtils.getLocaleFromFilename("some.ext");
		assertEquals(pathLocale.path, "some.ext");
		assertNull(pathLocale.locale);

		pathLocale = ResourceUtils.getLocaleFromFilename("some.min.ext");
		assertEquals(pathLocale.path, "some.min.ext");
		assertNull(pathLocale.locale);

		pathLocale = ResourceUtils.getLocaleFromFilename("some.min_en.ext");
		assertEquals(pathLocale.path, "some.min.ext");
		assertEquals(pathLocale.locale, Locale.ENGLISH);

		pathLocale = ResourceUtils.getLocaleFromFilename("some_fr_CA.min.ext");
		assertEquals(pathLocale.path, "some.min.ext");
		assertEquals(pathLocale.locale, Locale.CANADA_FRENCH);

		String localeVariant = "blah";
		pathLocale = ResourceUtils
			.getLocaleFromFilename("some_fr_CA_" + localeVariant + ".min.ext");
		assertEquals(pathLocale.path, "some.min.ext");
		assertEquals(pathLocale.locale.getLanguage(), "fr");
		assertEquals(pathLocale.locale.getCountry(), "CA");
		assertEquals(pathLocale.locale.getVariant(), localeVariant);
	}
}
