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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.examples.tree.Foo;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * @author Sven Meier
 */
public class EditableFolderContent extends Content
{

	private static final long serialVersionUID = 1L;

	@Override
	public Component newContentComponent(String id, final AbstractTree<Foo> tree, IModel<Foo> model)
	{
		return new Folder<Foo>(id, tree, model)
		{

			/**
			 * Always clickable.
			 */
			@Override
			protected boolean isClickable()
			{
				return true;
			}

			/**
			 * AjaxEditableLabel won't work reliable in Safari if wrapped in a Link, so simply
			 * replace the anchor with a &lt;span&gt;.
			 */
			@Override
			protected MarkupContainer newLinkComponent(String id, IModel<Foo> model)
			{
				return new WebMarkupContainer(id)
				{
					@Override
					protected void onComponentTag(ComponentTag tag)
					{
						tag.setName("span");
						super.onComponentTag(tag);
					}
				};
			}

			@Override
			protected Component newLabelComponent(String id, final IModel<Foo> model)
			{
				return new AjaxEditableLabel<String>(id, new PropertyModel<>(model, "bar"))
				{
					@Override
					protected void onSubmit(AjaxRequestTarget target)
					{
						super.onSubmit(target);

						/**
						 * update whole node in case we're located inside TableTree
						 */
						tree.updateNode(model.getObject(), target);
					}
				};
			}
		};
	}
}
