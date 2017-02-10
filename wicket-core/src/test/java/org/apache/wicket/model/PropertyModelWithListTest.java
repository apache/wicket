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
package org.apache.wicket.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-3929
 * 
 * @author Carl-Eric Menzel
 */
public class PropertyModelWithListTest extends WicketTestCase
{
	/** */
	public static class BeansContainer
	{
		private List<Bean> beans = new ArrayList<Bean>();

		/**
		 * @return the beans
		 * 
		 */
		public List<Bean> getBeans()
		{
			return beans;
		}

		/**
		 * @param beans
		 *            the bean
		 */
		public void setBeans(List<Bean> beans)
		{
			this.beans = beans;
		}
	}

	/** */
	public static class Bean
	{
		private String text;

		/**
		 * @return the bean's text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * @param text
		 *            the bean's text
		 */
		public void setText(String text)
		{
			this.text = text;
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void listPropertyModel() throws Exception
	{
		List<Bean> beans = new ArrayList<PropertyModelWithListTest.Bean>();
		Bean bean = new Bean();
		bean.setText("Wrinkly and green I am.");
		beans.add(bean);
		PropertyModel<String> model = new PropertyModel<String>(beans, "0.text");
		assertEquals("Wrinkly and green I am.", model.getObject());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void containerPropertyModel() throws Exception
	{
		BeansContainer container = new BeansContainer();
		Bean bean = new Bean();
		bean.setText("Wrinkly and green I am.");
		container.getBeans().add(bean);
		PropertyModel<String> model = new PropertyModel<String>(container, "beans[0].text");
		assertEquals("Wrinkly and green I am.", model.getObject());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void nestedListPropertyModel() throws Exception
	{
		List<List<Bean>> outer = new ArrayList<List<Bean>>();
		List<Bean> inner = new ArrayList<Bean>();
		outer.add(inner);
		Bean bean = new Bean();
		bean.setText("Wrinkly and green I am.");
		inner.add(bean);
		PropertyModel<String> model = new PropertyModel<String>(outer, "0[0].text");
		assertEquals("Wrinkly and green I am.", model.getObject());
	}
}