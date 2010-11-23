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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.ajax.builtin.BasePage;
import org.apache.wicket.markup.html.tree.AbstractTree;


/**
 * This is a base class for all pages with tree example.
 * 
 * @author Matej Knopp
 */
public abstract class BaseTreePage extends BasePage
{

	/**
	 * Default constructor
	 */
	public BaseTreePage()
	{
		add(new AjaxLink<Void>("expandAll")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				getTree().getTreeState().expandAll();
				getTree().updateTree(target);
			}
		});

		add(new AjaxLink<Void>("collapseAll")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				getTree().getTreeState().collapseAll();
				getTree().updateTree(target);
			}
		});

		add(new AjaxLink<Void>("switchRootless")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				getTree().setRootLess(!getTree().isRootLess());
				getTree().updateTree(target);
			}
		});
	}

	/**
	 * Returns the tree on this pages. This is used to collapse, expand the tree and to switch the
	 * rootless mode.
	 * 
	 * @return Tree instance on this page
	 */
	protected abstract AbstractTree getTree();

	/**
	 * Creates the model that feeds the tree.
	 * 
	 * @return New instance of tree model.
	 */
	protected TreeModel createTreeModel()
	{
		List<Object> l1 = new ArrayList<Object>();
		l1.add("test 1.1");
		l1.add("test 1.2");
		l1.add("test 1.3");
		List<Object> l2 = new ArrayList<Object>();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");
		List<Object> l3 = new ArrayList<Object>();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");

		l2.add(l3);

		l2.add("test 2.4");
		l2.add("test 2.5");
		l2.add("test 2.6");

		l3 = new ArrayList<Object>();
		l3.add("test 3.1");
		l3.add("test 3.2");
		l3.add("test 3.3");
		l2.add(l3);

		l1.add(l2);

		l2 = new ArrayList<Object>();
		l2.add("test 2.1");
		l2.add("test 2.2");
		l2.add("test 2.3");

		l1.add(l2);

		l1.add("test 1.3");
		l1.add("test 1.4");
		l1.add("test 1.5");

		return convertToTreeModel(l1);
	}

	private TreeModel convertToTreeModel(List<Object> list)
	{
		TreeModel model = null;
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new ModelBean("ROOT"));
		add(rootNode, list);
		model = new DefaultTreeModel(rootNode);
		return model;
	}

	private void add(DefaultMutableTreeNode parent, List<Object> sub)
	{
		for (Object obj : sub)
		{
			if (obj instanceof List)
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean(
						"subtree..."));
				parent.add(child);
				add(child, (List<Object>) obj);
			}
			else
			{
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ModelBean(
						obj.toString()));
				parent.add(child);
			}
		}
	}

}
