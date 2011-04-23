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
package org.apache.wicket.extensions.markup.html.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;

/**
 */
public class MoveChildToParentNodeMarkedForRecreationTestPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final Tree treeTable;
	DefaultMutableTreeNode c2;
	DefaultMutableTreeNode c3;

	/**
	 * Construct.
	 */
	public MoveChildToParentNodeMarkedForRecreationTestPage()
	{
		treeTable = new Tree("tree", getTreeModel());
		treeTable.getTreeState().expandAll();
		add(treeTable);
		add(new AjaxLink<Void>("moveC3ToC2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				treeTable.modelChanging();
				DefaultTreeModel model = (DefaultTreeModel)treeTable.getDefaultModelObject();
				model.removeNodeFromParent(c3);
				model.insertNodeInto(c3, c2, model.getChildCount(c2));
				treeTable.modelChanged();
				treeTable.nodeSelected(c2);
				treeTable.updateTree(target);
			}
		});
	}

	private javax.swing.tree.TreeModel getTreeModel()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
		c2 = new DefaultMutableTreeNode("c2");
		c3 = new DefaultMutableTreeNode("c3");
		DefaultMutableTreeNode cc2 = new DefaultMutableTreeNode("cc2");
		DefaultTreeModel model = new DefaultTreeModel(root);
		model.insertNodeInto(c1, root, 0);
		model.insertNodeInto(c2, root, 1);
		model.insertNodeInto(cc2, c2, 0);
		model.insertNodeInto(c3, root, 2);
		return model;
	}


}
