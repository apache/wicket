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
package org.apache.wicket.extensions.markup.html.repeater.tree;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.model.IModel;

/**
 * An implementation of the NestedTree that aims to solve the 90% usecase by using {@link Folder}s
 * on a standard {@link NestedTree}.
 * 
 * @param <T>
 *            The node type
 * @author svenmeier
 */
public class DefaultNestedTree<T> extends NestedTree<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param provider
	 *            provider of the tree
	 */
	public DefaultNestedTree(String id, ITreeProvider<T> provider)
	{
		this(id, provider, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            component id
	 * @param provider
	 *            provider of the tree
	 * @param state
	 *            expansion state
	 */
	public DefaultNestedTree(String id, ITreeProvider<T> provider, IModel<? extends Set<T>> state)
	{
		super(id, provider, state);

		add(new WindowsTheme());
	}

	/**
	 * Creates {@link Folder} for each node.
	 * 
	 * @param id
	 *            component id
	 * @param node
	 *            the node model
	 */
	@Override
	protected Component newContentComponent(String id, IModel<T> node)
	{
		return new Folder<>(id, this, node);
	}
}
