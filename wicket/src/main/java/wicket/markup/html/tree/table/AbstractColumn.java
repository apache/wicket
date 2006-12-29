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
package wicket.markup.html.tree.table;

import javax.swing.tree.TreeNode;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;

/**
 * Convenience class for building tree columns.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractColumn implements IColumn
{
	private String header;

	private ColumnLocation location;

	private TreeTable treeTable = null;

	/**
	 * Creates the tree column.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size
	 *            should be
	 * 
	 * @param header
	 *            Header caption
	 */
	public AbstractColumn(ColumnLocation location, String header)
	{
		this.location = location;
		this.header = header;
	}

	/**
	 * @see IColumn#getLocation()
	 */
	public ColumnLocation getLocation()
	{
		return location;
	}

	/**
	 * @see IColumn#getSpan(TreeNode)
	 */
	public int getSpan(TreeNode node)
	{
		return 0;
	}

	/**
	 * @see IColumn#isVisible()
	 */
	public boolean isVisible()
	{
		return true;
	}

	/**
	 * @see IColumn#newHeader(MarkupContainer, String)
	 */
	public Component newHeader(MarkupContainer<?> parent, String id)
	{
		return new Label(parent, id, header);
	}

	/**
	 * @see IColumn#setTreeTable(TreeTable)
	 */
	public void setTreeTable(TreeTable treeTable)
	{
		this.treeTable = treeTable;
	}

	/**
	 * Returns the tree table that this columns belongs to. If you call this
	 * method from constructor it will return null, as the column is constructed
	 * before the tree is.
	 * 
	 * @return The tree table this column belongs to
	 */
	protected TreeTable getTreeTable()
	{
		return treeTable;
	}
}
