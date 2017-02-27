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
package org.apache.wicket.util.lang;

import org.apache.wicket.core.util.lang.OGNLPropertyExpressionResolver;
import org.apache.wicket.core.util.lang.OGNLPropertyExpressionResolver.DefaultPropertyLocator;
import org.apache.wicket.core.util.lang.OGNLPropertyExpressionResolver.IPropertyLocator;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.core.util.lang.PropertyResolverConverter;
import org.apache.wicket.core.util.reflection.AbstractGetAndSet;
import org.apache.wicket.core.util.reflection.IGetAndSet;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

public class OGNLPropertyExpressionResolverTest extends WicketTestCase
{
	private OGNLPropertyExpressionResolver ognlResolver = new OGNLPropertyExpressionResolver(
		new CustomGetAndSetLocator());

	/**
	 * WICKET-5623 custom properties
	 */
	@Test
	public void custom()
	{
		tester.getApplication().getApplicationSettings()
			.setPropertyExpressionResolver(ognlResolver);

		Document document = new Document();
		document.setType("type");
		document.setProperty("string", "string");

		Document nestedCustom = new Document();
		nestedCustom.setProperty("string", "string2");
		document.setProperty("nested", nestedCustom);

		assertEquals("type", PropertyResolver.getValue("type", document));
		assertEquals("string", PropertyResolver.getValue("string", document));
		assertEquals("string2", PropertyResolver.getValue("nested.string", document));
	}

	class CustomGetAndSetLocator implements IPropertyLocator
	{

		private IPropertyLocator locator = new DefaultPropertyLocator();

		@Override
		public IGetAndSet get(Class<?> clz, String exp)
		{
			// first try default properties
			IGetAndSet getAndSet = locator.get(clz, exp);
			if (getAndSet == null && Document.class.isAssignableFrom(clz))
			{
				// fall back to document properties
				getAndSet = new DocumentPropertyGetAndSet(exp);
			}
			return getAndSet;
		}

		public class DocumentPropertyGetAndSet extends AbstractGetAndSet
		{

			private String name;

			public DocumentPropertyGetAndSet(String name)
			{
				this.name = name;
			}

			@Override
			public Object getValue(Object object)
			{
				return ((Document)object).getProperty(name);
			}

			@Override
			public Object newValue(Object object)
			{
				return new Document();
			}

			@Override
			public void setValue(Object object, Object value, PropertyResolverConverter converter)
			{
				((Document)object).setProperty(name, value);
			}
		}
	}
}
