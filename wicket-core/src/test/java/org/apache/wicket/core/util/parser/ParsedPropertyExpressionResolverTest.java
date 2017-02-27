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
package org.apache.wicket.core.util.parser;

import static org.hamcrest.CoreMatchers.is;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.ConverterLocator;
import org.apache.wicket.core.util.lang.IPropertyExpressionResolver;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.util.lang.Person;
import org.apache.wicket.util.lang.WeirdList;
import org.apache.wicket.util.lang.WeirdMap;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

public class ParsedPropertyExpressionResolverTest extends WicketTestCase
{

	private static final Integer AN_INTEGER = 10;
	private static final Integer ANOTHER_INTEGER = 11;
	private static final PropertyResolverConverter CONVERTER = new PropertyResolverConverter(
		new ConverterLocator(), Locale.US);

	private IPropertyExpressionResolver resolver = new ParsedPropertyExpressionResolver();
	private Person person = new Person();
	private Map<String, Integer> integerMap = new HashMap<String, Integer>();
	private WeirdMap map = new WeirdMap();
	private WeirdList list = new WeirdList();

	@Before
	public void before()
	{
		tester.getApplication().getApplicationSettings().setPropertyExpressionResolver(resolver);
	}

	@Test
	public void shouldAllowEmptySpacesInsideMethodCallBrackets() throws Exception
	{
		person.setName("bob");
		assertThat("bob", is(PropertyResolver.getValue("person.getName( )", this)));
	}

	@Test
	public void shouldAllowMapKeysWithSpecialCharactersIncludingOpenSquareBracket() throws Exception
	{
		String code = "!@#$%^&*()_+-=[{}|";
		String expression = "[" + code + "]";
		PropertyResolver.setValue(expression, integerMap, AN_INTEGER, CONVERTER);
		assertThat(PropertyResolver.getValue(expression, integerMap), is(AN_INTEGER));
		assertThat(integerMap.get(code), is(AN_INTEGER));
	}

	@Test
	public void shouldGetValueAtAListPosition() throws Exception
	{
		list.add(null);
		PropertyResolver.setValue("valueAt.0", list, AN_INTEGER, CONVERTER);
		assertThat(list.get(0), is(AN_INTEGER));
	}

	@Test
	public void shouldGetValueAtAMapPosition() throws Exception
	{
		PropertyResolver.setValue("valueAt.0", map, AN_INTEGER, CONVERTER);
		assertThat(map.getValueAt(0), is(AN_INTEGER));
	}
	
	/**
	 * https://issues.apache.org/jira/browse/WICKET-6327
	 */
	@Test
	public void shouldGetValueInAField() throws Exception
	{
		map.field = AN_INTEGER;
		map.put("field", ANOTHER_INTEGER);
		list.field = AN_INTEGER;
		assertThat(PropertyResolver.getValue("map.field", this), is(AN_INTEGER));
		assertThat(PropertyResolver.getValue("map[field]", this), is(ANOTHER_INTEGER));
		assertThat(PropertyResolver.getValue("list.field", this), is(AN_INTEGER));
		
	}

}
