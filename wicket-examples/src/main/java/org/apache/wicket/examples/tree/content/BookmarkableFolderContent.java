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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.examples.tree.Foo;
import org.apache.wicket.examples.tree.TreeApplication;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author Sven Meier
 */
public class BookmarkableFolderContent extends Content
{

	private static final long serialVersionUID = 1L;

	public BookmarkableFolderContent(final AbstractTree<Foo> tree)
	{
		String id = tree.getRequest().getRequestParameters().getParameterValue("foo").toString();
		if (id != null)
		{
			Foo foo = TreeApplication.get().getFoo(id);
			while (foo != null)
			{
				tree.getModel().getObject().add(foo);
				foo = foo.getParent();
			}
		}
	}

	@Override
	public Component newContentComponent(String id, final AbstractTree<Foo> tree, IModel<Foo> model)
	{
		return new Folder<Foo>(id, tree, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected MarkupContainer newLinkComponent(String id, IModel<Foo> model)
			{
				Foo foo = model.getObject();

				if (tree.getProvider().hasChildren(foo))
				{
					return super.newLinkComponent(id, model);
				}
				else
				{
					PageParameters parameters = new PageParameters();
					parameters.add("foo", foo.getId());

					return new BookmarkablePageLink<>(id, tree.getPage().getClass(), parameters);
				}
			}
		};
	}
}
