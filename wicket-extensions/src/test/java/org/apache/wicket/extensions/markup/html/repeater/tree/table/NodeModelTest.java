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

import junit.framework.Assert;

import org.apache.wicket.model.IWriteableModel;
import org.junit.Test;

/**
 * Test for {@link NodeModel}.
 * 
 * @author svenmeier
 */
public class NodeModelTest extends Assert
{
	/**
	 * Test equality.
	 */
	@Test
	public void equality()
	{
		NodeModel<String> model = new NodeModel<String>(new StringModel("A"), new boolean[] { true,
				false });

		assertTrue(model.equals(new NodeModel<String>(new StringModel("A"), new boolean[] { true,
				false })));

		assertFalse(model.equals(new NodeModel<String>(new StringModel("A"), new boolean[] { true,
				true })));

		assertFalse(model.equals(new NodeModel<String>(new StringModel("B"), new boolean[] { true,
				false })));
	}

	private class StringModel implements IWriteableModel<String>
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
		public void detach()
		{
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