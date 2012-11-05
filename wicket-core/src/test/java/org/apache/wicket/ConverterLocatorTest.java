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
package org.apache.wicket;

import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for converter locators.
 * 
 * @author Eelco Hillenius
 */
public final class ConverterLocatorTest extends Assert
{


	/**
	 * Test generalized conversion
	 */
	@Test
	public void test()
	{
		final IConverterLocator locator = new ConverterLocator();
		assertNotNull(locator.getConverter(Integer.class));
		assertNotNull(locator.getConverter(Double.class));

		// default converter
		assertNotNull(locator.getConverter(String.class).convertToObject("", Locale.US));
	}


	/**
	 * Verifies that a new instance of date converter is returned
	 * if there is no custom converter registered.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4839
	 */
	@Test
	public void customDateConverter()
	{
		final ConverterLocator locator = new ConverterLocator();

		/**
		 * A custom converter that can override the default
		 * registered DateConverter
		 */
		class CustomDateConverter extends DateConverter
		{
		}

		IConverter<Date> dateConverter = locator.getConverter(Date.class);

		// assert that a DateConverter is returned
		assertSame(DateConverter.class, dateConverter.getClass());
		assertNotSame(CustomDateConverter.class, dateConverter.getClass());

		IConverter<Date> secondDateConverter = locator.getConverter(Date.class);

		// assert that a new instance of DateConverter is returned
		assertNotSame(dateConverter, secondDateConverter);

		locator.set(Date.class, new CustomDateConverter());
		dateConverter = locator.getConverter(Date.class);

		// assert that the CustomDateConverter is returned
		assertNotSame(DateConverter.class, dateConverter.getClass());
		assertSame(CustomDateConverter.class, dateConverter.getClass());

	}
}