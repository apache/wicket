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
package org.apache.wicket.extensions.markup.html.repeater.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link ProviderSubset}.
 * 
 * @author svenmeier
 */
public class ProviderSubsetTest extends Assert
{
	private ITreeProvider<String> provider = new EmptyProvider();

	/**
	 * All models requested from the provider.
	 */
	private List<StringModel> models = new ArrayList<>();

	/**
	 * Test set methods.
	 */
	@Test
	public void setMethods()
	{
		ProviderSubset<String> subset = new ProviderSubset<>(provider);

		subset.add("A");
		subset.addAll(Arrays.asList("AA", "AAA"));

		assertEquals(3, subset.size());

		Iterator<String> iterator = subset.iterator();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertTrue(iterator.hasNext());
		iterator.next();
		assertFalse(iterator.hasNext());
		try
		{
			iterator.next();
			fail();
		}
		catch (Exception expected)
		{
		}

		assertTrue(subset.contains("A"));
		assertTrue(subset.contains("AA"));
		assertTrue(subset.contains("AAA"));

		subset.createModel().detach();

		for (StringModel model : models)
		{
			assertTrue(model.isDetached());
		}

		assertTrue(subset.contains("A"));
		assertTrue(subset.contains("AA"));
		assertTrue(subset.contains("AAA"));
	}

	private class StringModel implements IModel<String>
	{

		private static final long serialVersionUID = 1L;

		private String string;

		private boolean detached;

		public StringModel(String string)
		{
			this.string = string;
			models.add(this);
		}

		@Override
		public String getObject()
		{
			detached = false;
			return string;
		}

		@Override
		public void setObject(String string)
		{
			detached = false;
			this.string = string;
		}

		@Override
		public void detach()
		{
			detached = true;
		}

		public boolean isDetached()
		{
			return detached;
		}

		@Override
		public boolean equals(Object obj)
		{
			return string == ((StringModel)obj).string;
		}

		@Override
		public int hashCode()
		{
			return string.hashCode();
		}
	}

	private class EmptyProvider implements ITreeProvider<String>
	{

		private static final long serialVersionUID = 1L;

		private List<String> EMPTY = new ArrayList<>();

		@Override
		public Iterator<String> getRoots()
		{
			return EMPTY.iterator();
		}

		@Override
		public boolean hasChildren(String object)
		{
			return false;
		}

		@Override
		public Iterator<String> getChildren(String string)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public IModel<String> model(String string)
		{
			return new StringModel(string);
		}

		@Override
		public void detach()
		{
		}
	}
}
