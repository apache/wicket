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
package org.apache.wicket.examples.tree;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * A provider of {@link Foo}s.
 * 
 * For simplicity all foos are kept as class members, in a real world scenario these would be
 * fetched from a database. If {@link Foo}s were {@link Serializable} you could of course just keep
 * references in instance variables.
 * 
 * @see #model(Foo)
 * 
 * @author Sven Meier
 */
public class FooProvider implements ITreeProvider<Foo>
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public FooProvider()
	{
	}

	/**
	 * Nothing to do.
	 */
	@Override
	public void detach()
	{
	}

	@Override
	public Iterator<Foo> getRoots()
	{
		return TreeApplication.get().foos.iterator();
	}

	@Override
	public boolean hasChildren(Foo foo)
	{
		return foo.getParent() == null || !foo.getFoos().isEmpty();
	}

	@Override
	public Iterator<Foo> getChildren(final Foo foo)
	{
		return foo.getFoos().iterator();
	}

	/**
	 * Creates a {@link FooModel}.
	 */
	@Override
	public IModel<Foo> model(Foo foo)
	{
		return new FooModel(foo);
	}

	/**
	 * A {@link Model} which uses an id to load its {@link Foo}.
	 * 
	 * If {@link Foo}s were {@link Serializable} you could just use a standard {@link Model}.
	 * 
	 * @see #equals(Object)
	 * @see #hashCode()
	 */
	private static class FooModel extends LoadableDetachableModel<Foo>
	{
		private static final long serialVersionUID = 1L;

		private final String id;

		public FooModel(Foo foo)
		{
			super(foo);

			id = foo.getId();
		}

		@Override
		protected Foo load()
		{
			return TreeApplication.get().getFoo(id);
		}

		/**
		 * Important! Models must be identifyable by their contained object.
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof FooModel)
			{
				return ((FooModel)obj).id.equals(id);
			}
			return false;
		}

		/**
		 * Important! Models must be identifyable by their contained object.
		 */
		@Override
		public int hashCode()
		{
			return id.hashCode();
		}
	}
}
