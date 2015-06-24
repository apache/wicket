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


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * WICKET-2035 Change naming convention for xml properties files to *.properties.xml instead of
 * *.xml
 */
public class XmlFilePropertiesLoaderTest extends WicketTestCase
{
	static Map<Locale, String> EXPECTED_LOCALIZATIONS = new HashMap<Locale, String>();
	static
	{
		EXPECTED_LOCALIZATIONS.put(Locale.US, "value");
		EXPECTED_LOCALIZATIONS.put(Locale.FRANCE, "valeur");
	}

	/**
	 * Tests that the localizations for {@link PageWithXmlProperties} are successfully loaded from
	 * (PageWithXmlProperties_locale).properties.xml
	 */
	@Test
	public void wicket2035()
	{
		for (Locale locale : EXPECTED_LOCALIZATIONS.keySet())
		{
			tester.getSession().setLocale(locale);
			tester.startPage(PageWithXmlProperties.class);
		}
	}
}
