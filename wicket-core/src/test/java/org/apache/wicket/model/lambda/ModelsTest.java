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
package org.apache.wicket.model.lambda;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Test;

public class ModelsTest
{
	@Test
	public void propertyModel() {
		Foo instance = new Foo();
		Model<Foo> fooModel = Model.of(instance);
		IModel<String> nameModel = Models.of(fooModel, Foo::setName, Foo::getName);
		instance.setName("blub");
		assertEquals("blub", nameModel.getObject());
		nameModel.setObject("bar");
		assertEquals("bar", instance.getName());
	}
	
	static class Foo implements Serializable {
		
		String name;
		
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
	}
}
