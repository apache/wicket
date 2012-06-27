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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.examples.tree.content.BookmarkableFolderContent;
import org.apache.wicket.examples.tree.content.CheckedFolderContent;
import org.apache.wicket.examples.tree.content.CheckedSelectableFolderContent;
import org.apache.wicket.examples.tree.content.Content;
import org.apache.wicket.examples.tree.content.EditableFolderContent;
import org.apache.wicket.examples.tree.content.FolderContent;
import org.apache.wicket.examples.tree.content.LabelContent;
import org.apache.wicket.examples.tree.content.MixedContent;
import org.apache.wicket.examples.tree.content.MultiLineLabelContent;
import org.apache.wicket.examples.tree.content.MultiSelectableFolderContent;
import org.apache.wicket.examples.tree.content.PanelContent;
import org.apache.wicket.examples.tree.content.SelectableFolderContent;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * @author Sven Meier
 */
public abstract class TreePage extends WicketExamplePage
{

	private static final long serialVersionUID = 1L;

	private Behavior theme;

	private AbstractTree<Foo> tree;

	private FooProvider provider = new FooProvider();

	private Content content;

	private List<Content> contents;

	private List<Behavior> themes;

	/**
	 * Construct.
	 */
	public TreePage()
	{
		content = new CheckedFolderContent(provider);

		Form<Void> form = new Form<Void>("form");
		add(form);

		tree = createTree(provider, new FooExpansionModel());
		tree.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onComponentTag(Component component, ComponentTag tag)
			{
				theme.onComponentTag(component, tag);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response)
			{
				theme.renderHead(component, response);
			}
		});
		form.add(tree);

		form.add(new DropDownChoice<Content>("content",
			new PropertyModel<Content>(this, "content"), initContents(),
			new ChoiceRenderer<Content>("class.simpleName"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		});

		form.add(new DropDownChoice<Behavior>("theme", new PropertyModel<Behavior>(this, "theme"),
			initThemes(), new ChoiceRenderer<Behavior>("class.simpleName"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		});

		form.add(new Link<Void>("expandAll")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				FooExpansion.get().expandAll();
			}
		});

		form.add(new Link<Void>("collapseAll")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				FooExpansion.get().collapseAll();
			}
		});

		form.add(new Button("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
			}
		});
	}

	protected abstract AbstractTree<Foo> createTree(FooProvider provider, IModel<Set<Foo>> state);

	private List<Content> initContents()
	{
		contents = new ArrayList<Content>();

		contents.add(new BookmarkableFolderContent(tree));
		contents.add(new LabelContent());
		contents.add(new MultiLineLabelContent());
		contents.add(new FolderContent());
		contents.add(new EditableFolderContent());
		contents.add(new SelectableFolderContent(provider));
		contents.add(new MultiSelectableFolderContent(provider));
		contents.add(new CheckedFolderContent(provider));
		contents.add(new CheckedSelectableFolderContent(provider));
		contents.add(new PanelContent());
		contents.add(new MixedContent(contents));

		content = contents.get(0);

		return contents;
	}

	private List<Behavior> initThemes()
	{
		themes = new ArrayList<Behavior>();

		themes.add(new WindowsTheme());
		themes.add(new HumanTheme());

		theme = themes.get(0);

		return themes;
	}

	@Override
	public void detachModels()
	{
		for (Content content : contents)
		{
			content.detach();
		}

		super.detachModels();
	}

	protected Component newContentComponent(String id, IModel<Foo> model)
	{
		return content.newContentComponent(id, tree, model);
	}

	private class FooExpansionModel extends AbstractReadOnlyModel<Set<Foo>>
	{
		@Override
		public Set<Foo> getObject()
		{
			return FooExpansion.get();
		}
	}
}
