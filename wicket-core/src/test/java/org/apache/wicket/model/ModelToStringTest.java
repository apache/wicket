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

import java.util.Properties;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;


/**
 * Tests the toString() method on the models in the org.apache.wicket.model package.
 */
public class ModelToStringTest extends WicketTestCase
{
	/**
	 * Used for models in testing.
	 */
	private static class InnerPOJO
	{
		@Override
		public String toString()
		{
			return "pojo";
		}
	}

	/**
	 * Test stub for testing AbstractReadOnlyModel.toString()
	 */
	private static class MyAbstractReadOnlyModel extends AbstractReadOnlyModel<String>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see AbstractReadOnlyModel#getObject()
		 */
		@Override
		public String getObject()
		{
			return "FOO";
		}
	}

	private static final class MyLoadableDetachableModel extends LoadableDetachableModel<String>
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected String load()
		{
			return "foo";
		}
	}

	/**
	 * Tests AbstractReadOnlyModel.toString().
	 */
	@Test
	public void abstractReadOnlyModel()
	{
		AbstractReadOnlyModel<String> model = new MyAbstractReadOnlyModel();
		String expected = "Model:classname=[" + model.getClass().getName() + "]";
		assertEquals(expected, model.toString());
	}

	/**
	 * Tests the BoundCompoundPropertyModel.toString() method.
	 */
	@Test
	public void boundCompoundPropertyModel()
	{
		CompoundPropertyModel<String> emptyModel = new CompoundPropertyModel<String>("");
		String expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[]";
		assertEquals(expected, emptyModel.toString());

		Properties properties = new Properties();
		properties.put("name", "foo");
		CompoundPropertyModel<Properties> stringProperty = new CompoundPropertyModel<Properties>(
			properties);

		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[{name=foo}]";
		assertEquals(expected, stringProperty.toString());

		stringProperty.getObject();
		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[{name=foo}]";
		assertEquals(expected, stringProperty.toString());

		InnerPOJO innerPOJO = new InnerPOJO();
		CompoundPropertyModel<InnerPOJO> pojoProperty = new CompoundPropertyModel<InnerPOJO>(
			innerPOJO);

		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[" +
			innerPOJO + "]";
		assertEquals(expected, pojoProperty.toString());
	}

	/**
	 * Tests the CompoundPropertyModel.toString() method.
	 */
	@Test
	public void compoundPropertyModel()
	{
		CompoundPropertyModel<?> emptyModel = new CompoundPropertyModel<String>("");
		String expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[]";
		assertEquals(expected, emptyModel.toString());

		Properties properties = new Properties();
		properties.put("name", "foo");
		CompoundPropertyModel<Properties> stringProperty = new CompoundPropertyModel<Properties>(
			properties);

		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[{name=foo}]";
		assertEquals(expected, stringProperty.toString());

		stringProperty.getObject();
		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[{name=foo}]";
		assertEquals(expected, stringProperty.toString());

		InnerPOJO innerPOJO = new InnerPOJO();
		CompoundPropertyModel<InnerPOJO> pojoProperty = new CompoundPropertyModel<InnerPOJO>(
			innerPOJO);

		expected = "Model:classname=[org.apache.wicket.model.CompoundPropertyModel]:nestedModel=[" +
			innerPOJO + "]";
		assertEquals(expected, pojoProperty.toString());
	}

	/**
	 * Tests LoadableDetachableModel.toString()
	 */
	@Test
	public void loadableDetachableModel()
	{
		LoadableDetachableModel<String> model = new MyLoadableDetachableModel();
		assertTrue(model.toString().contains(":attached=false"));
		assertTrue(model.toString().contains(":tempModelObject=[null]"));

		model.getObject();
		assertTrue(model.toString().contains(":attached=true"));
		assertTrue(model.toString().contains(":tempModelObject=[foo]"));

		model.detach();
		assertTrue(model.toString().contains(":attached=false"));
		assertTrue(model.toString().contains(":tempModelObject=[null]"));
	}


	/**
	 * Tests the Model.toString() method.
	 */
	@Test
	public void model()
	{
		Model<?> emptyModel = new Model<String>();
		String expected = "Model:classname=[org.apache.wicket.model.Model]:object=[null]";
		assertEquals(expected, emptyModel.toString());

		Model<String> stringModel = new Model<String>("foo");
		expected = "Model:classname=[org.apache.wicket.model.Model]:object=[foo]";
		assertEquals(expected, stringModel.toString());
	}

	/**
	 * Tests the PropertyModel.toString() method.
	 */
	@Test
	public void propertyModel()
	{
		PropertyModel<Void> emptyModel = new PropertyModel<Void>("", null);
		String expected = "Model:classname=[org.apache.wicket.model.PropertyModel]:nestedModel=[]:expression=[null]";
		assertEquals(expected, emptyModel.toString());

		Properties properties = new Properties();
		properties.put("name", "foo");
		PropertyModel<String> stringProperty = new PropertyModel<String>(properties, "name");

		expected = "Model:classname=[org.apache.wicket.model.PropertyModel]:nestedModel=[{name=foo}]:expression=[name]";
		assertEquals(expected, stringProperty.toString());

		stringProperty.getObject();
		expected = "Model:classname=[org.apache.wicket.model.PropertyModel]:nestedModel=[{name=foo}]:expression=[name]";
		assertEquals(expected, stringProperty.toString());

		InnerPOJO innerPOJO = new InnerPOJO();
		PropertyModel<?> pojoProperty = new PropertyModel<Object>(innerPOJO, "pojo");

		expected = "Model:classname=[org.apache.wicket.model.PropertyModel]:nestedModel=[pojo]:expression=[pojo]";
		assertEquals(expected, pojoProperty.toString());
	}
}
