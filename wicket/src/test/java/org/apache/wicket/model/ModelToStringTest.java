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

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;


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
	 * Construct.
	 * 
	 * @param name
	 */
	public ModelToStringTest(String name)
	{
		super(name);
	}

	/**
	 * Tests AbstractReadOnlyModel.toString().
	 */
	public void testAbstractReadOnlyModel()
	{
		AbstractReadOnlyModel<String> model = new MyAbstractReadOnlyModel();
		String expected = "Model:classname=[" + model.getClass().getName() + "]";
		assertEquals(expected, model.toString());
	}

	/**
	 * Tests the BoundCompoundPropertyModel.toString() method.
	 */
	@SuppressWarnings("deprecation")
	public void testBoundCompoundPropertyModel()
	{
		BoundCompoundPropertyModel<String> emptyModel = new BoundCompoundPropertyModel<String>("");
		String expected = "Model:classname=[org.apache.wicket.model.BoundCompoundPropertyModel]:nestedModel=[]:bindings=[]";
		assertEquals(expected, emptyModel.toString());

		Properties properties = new Properties();
		properties.put("name", "foo");
		BoundCompoundPropertyModel<String> stringProperty = new BoundCompoundPropertyModel<String>(
			properties);

		expected = "Model:classname=[org.apache.wicket.model.BoundCompoundPropertyModel]:nestedModel=[{name=foo}]:bindings=[]";
		assertEquals(expected, stringProperty.toString());

		stringProperty.getObject();
		expected = "Model:classname=[org.apache.wicket.model.BoundCompoundPropertyModel]:nestedModel=[{name=foo}]:bindings=[]";
		assertEquals(expected, stringProperty.toString());

		InnerPOJO innerPOJO = new InnerPOJO();
		BoundCompoundPropertyModel<InnerPOJO> pojoProperty = new BoundCompoundPropertyModel<InnerPOJO>(
			innerPOJO);

		expected = "Model:classname=[org.apache.wicket.model.BoundCompoundPropertyModel]:nestedModel=[" +
			innerPOJO + "]:bindings=[]";
		assertEquals(expected, pojoProperty.toString());

		Component<?> component1 = pojoProperty.bind(new Label<Object>("label"));
		expected = "Model:classname=[org.apache.wicket.model.BoundCompoundPropertyModel]:nestedModel=[" +
			innerPOJO + "]:bindings=[Binding(:component=[" + component1 + "]:expression=[label])]";
		assertEquals(expected, pojoProperty.toString());
	}

	/**
	 * Tests the CompoundPropertyModel.toString() method.
	 */
	public void testCompoundPropertyModel()
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
	public void testLoadableDetachableModel()
	{
		LoadableDetachableModel<String> model = new MyLoadableDetachableModel();
		String expected = "Model:classname=[" + model.getClass().getName() + "]" +
			":attached=false" + ":tempModelObject=[null]";
		assertEquals(expected, model.toString());

		model.getObject();
		expected = "Model:classname=[" + model.getClass().getName() + "]" + ":attached=true" +
			":tempModelObject=[foo]";
		assertEquals(expected, model.toString());

		model.detach();
		expected = "Model:classname=[" + model.getClass().getName() + "]" + ":attached=false" +
			":tempModelObject=[null]";
		assertEquals(expected, model.toString());
	}


	/**
	 * Tests the Model.toString() method.
	 */
	public void testModel()
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
	public void testPropertyModel()
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
