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
package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.wicket.model.IModel;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link NodeModel}.
 * 
 * @author svenmeier
 */
public class NodeModelTest
{
	/**
	 * Test equality.
	 */
	@Test
	public void equality()
	{
		NodeModel<String> model = new NodeModel<>(new StringModel("A"),
			new boolean[] { true, false });

		assertEquals(model, new NodeModel<>(new StringModel("A"), new boolean[] { true, false }));

		assertNotEquals(model, new NodeModel<>(new StringModel("A"), new boolean[] { true, true }));

		assertNotEquals(model,
			new NodeModel<>(new StringModel("B"), new boolean[] { true, false }));
	}

	private class StringModel implements IModel<String>
	{

		private static final long serialVersionUID = 1L;

		private String string;

		public StringModel(String string)
		{
			this.string = string;
		}

		@Override
		public String getObject()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setObject(String object)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof StringModel)
			{
				return string.equals(((StringModel)obj).string);
			}

			return false;
		}
	}
}
