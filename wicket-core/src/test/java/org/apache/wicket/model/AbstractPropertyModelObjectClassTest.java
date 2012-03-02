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

import java.io.Serializable;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * If AbstractPropertyModel has an target that implements the IObjectClassAwareModel interface then
 * the class of that target is used to infer the modeled property type.
 * </p>
 * 
 * @see <a href="https://issues.apache.org/jira/browse/WICKET-2937">WICKET-2937</a>
 * @author Pedro Santos
 */
public class AbstractPropertyModelObjectClassTest extends Assert
{

	/**
	 * 
	 */
	@Test
	public void testCompoundPropertyModel()
	{
		assertPropertyModelTargetTypeIsInteger(new CompoundPropertyModel<CustomType>(
			new CustomType()));
	}

	/**
	 * 
	 */
	@Test
	public void testCompoundPropertyModelBind()
	{
		CompoundPropertyModel<CustomBean> compoundPropertyModel = new CompoundPropertyModel<CustomBean>(
			new CustomBean());
		IModel<?> modelForCustomTypeObject = compoundPropertyModel.bind("customType");
		assertPropertyModelTargetTypeIsInteger(modelForCustomTypeObject);
	}

	/**
	 * 
	 */
	@Test
	public void testModel()
	{
		assertPropertyModelTargetTypeIsInteger(new Model<CustomType>(new CustomType()));
	}

	/**
	 * Just asserting that the the property expression for the somePropety is aware of this property
	 * type.
	 * 
	 * @param modelForCustomTypeObject
	 */
	private void assertPropertyModelTargetTypeIsInteger(IModel<?> modelForCustomTypeObject)
	{
		assertEquals(Integer.class, new PropertyModel<IModel<?>>(modelForCustomTypeObject,
			"someProperty").getObjectClass());
	}

	/**
	 * Asserting that there is no problem in working with an AbstractPropertyModel targeting an
	 * IObjectClassAwareModel not initialized with an known class.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3253">WICKET-3253</a>
	 */
	@Test
	public void testLazyClassResolution()
	{
		Model<CustomBean> modelCustomBean = new Model<CustomBean>(null);
		PropertyModel<CustomType> customTypeModel = new PropertyModel<CustomType>(modelCustomBean,
			"customType");
		PropertyModel<Integer> somePropertyModel = new PropertyModel<Integer>(customTypeModel,
			"someProperty");
		assertNull(somePropertyModel.getObjectClass());
		modelCustomBean.setObject(new CustomBean());
		assertEquals(Integer.class, somePropertyModel.getObjectClass());
	}

	private static class CustomType implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private Integer someProperty;

		public void setSomeProperty(Integer someProperty)
		{
			this.someProperty = someProperty;
		}

		public Integer getSomeProperty()
		{
			return someProperty;
		}
	}

	private static class CustomBean implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private CustomType customType;

		public CustomType getCustomType()
		{
			return customType;
		}

		public void setCustomType(CustomType customType)
		{
			this.customType = customType;
		}
	}
}