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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class ResourceUtilsTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5706
	 */
	@Test
	public void getLocaleFromFilename()
	{
		ResourceUtils.PathLocale pathLocale;

		pathLocale = ResourceUtils.getLocaleFromFilename("some.ext");
		assertThat(pathLocale.path, is(equalTo("some.ext")));
		assertThat(pathLocale.locale, is(nullValue()));

		pathLocale = ResourceUtils.getLocaleFromFilename("some.min.ext");
		assertThat(pathLocale.path, is(equalTo("some.min.ext")));
		assertThat(pathLocale.locale, is(nullValue()));

		pathLocale = ResourceUtils.getLocaleFromFilename("some.min_en.ext");
		assertThat(pathLocale.path, is(equalTo("some.min.ext")));
		assertThat(pathLocale.locale, is(Locale.ENGLISH));

		pathLocale = ResourceUtils.getLocaleFromFilename("some.min_fr_CA.ext");
		assertThat(pathLocale.path, is(equalTo("some.min.ext")));
		assertThat(pathLocale.locale, is(Locale.CANADA_FRENCH));

		String localeVariant = "blah";
		pathLocale = ResourceUtils.getLocaleFromFilename("some.min_fr_CA_"+localeVariant+".ext");
		assertThat(pathLocale.path, is(equalTo("some.min.ext")));
		assertThat(pathLocale.locale.getLanguage(), is("fr"));
		assertThat(pathLocale.locale.getCountry(), is("CA"));
		assertThat(pathLocale.locale.getVariant(), is(localeVariant));
	}
}
