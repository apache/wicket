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
package org.apache.wicket.examples.tree.content;

import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.examples.tree.Foo;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.model.IModel;

/**
 * @author Sven Meier
 */
public class SelectableFolderContent extends Content
{

	private static final long serialVersionUID = 1L;

	private ITreeProvider<Foo> provider;

	private IModel<Foo> selected;

	public SelectableFolderContent(ITreeProvider<Foo> provider)
	{
		this.provider = provider;
	}

	@Override
	public void detach()
	{
		if (selected != null)
		{
			selected.detach();
		}
	}

	protected boolean isSelected(Foo foo)
	{
		IModel<Foo> model = provider.model(foo);

		try
		{
			return selected != null && selected.equals(model);
		}
		finally
		{
			model.detach();
		}
	}

	protected void select(Foo foo, AbstractTree<Foo> tree, final Optional<AjaxRequestTarget> targetOptional)
	{
		if (selected != null)
		{
			tree.updateNode(selected.getObject(), targetOptional);

			selected.detach();
			selected = null;
		}

		selected = provider.model(foo);

		tree.updateNode(foo, targetOptional);
	}

	@Override
	public Component newContentComponent(String id, final AbstractTree<Foo> tree, IModel<Foo> model)
	{
		return new Folder<Foo>(id, tree, model)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * Always clickable.
			 */
			@Override
			protected boolean isClickable()
			{
				return true;
			}

			@Override
			protected void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				SelectableFolderContent.this.select(getModelObject(), tree, targetOptional);
			}

			@Override
			protected boolean isSelected()
			{
				return SelectableFolderContent.this.isSelected(getModelObject());
			}
		};
	}
}
