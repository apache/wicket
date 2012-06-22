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
package org.apache.wicket.examples.ajax.builtin.tree;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.PropertyRenderableColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


/**
 * Column, that either shows a readonly cell or an editable panel, depending on whether the current
 * row is selected.
 * 
 * @author Matej Knopp
 * @param <T>
 *            the type of the property that is rendered in this column
 */
public class PropertyEditableColumn<T> extends PropertyRenderableColumn<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Column constructor.
	 * 
	 * @param location
	 * @param header
	 * @param propertyExpression
	 */
	public PropertyEditableColumn(ColumnLocation location, String header, String propertyExpression)
	{
		this(location, Model.of(header), propertyExpression);
	}

	/**
	 * Column constructor.
	 *
	 * @param location
	 * @param header
	 * @param propertyExpression
	 */
	public PropertyEditableColumn(ColumnLocation location, IModel<String> header, String propertyExpression)
	{
		super(location, header, propertyExpression);
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	@Override
	public Component newCell(MarkupContainer parent, String id, TreeNode node, int level)
	{
		return new EditablePanel(id, new PropertyModel<T>(node, getPropertyExpression()));
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	@Override
	public IRenderable newCell(TreeNode node, int level)
	{
		if (getTreeTable().getTreeState().isNodeSelected(node))
			return null;
		else
			return super.newCell(node, level);
	}
}