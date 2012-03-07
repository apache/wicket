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
package org.apache.wicket.extensions.markup.html.repeater.util;

import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link TreeModelProvider}.
 * 
 * @author svenmeier
 */
public class TreeModelProviderTest extends Assert
{
	private DefaultMutableTreeNode root;

	private DefaultTreeModel treeModel;

	/**
	 * Construct.
	 */
	public TreeModelProviderTest()
	{
		root = new DefaultMutableTreeNode("JTree");
		DefaultMutableTreeNode parent;

		parent = new DefaultMutableTreeNode("colors");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("blue"));
		parent.add(new DefaultMutableTreeNode("violet"));
		parent.add(new DefaultMutableTreeNode("red"));
		parent.add(new DefaultMutableTreeNode("yellow"));

		parent = new DefaultMutableTreeNode("sports");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("basketball"));
		parent.add(new DefaultMutableTreeNode("soccer"));
		parent.add(new DefaultMutableTreeNode("football"));
		parent.add(new DefaultMutableTreeNode("hockey"));

		parent = new DefaultMutableTreeNode("food");
		root.add(parent);
		parent.add(new DefaultMutableTreeNode("hot dogs"));
		parent.add(new DefaultMutableTreeNode("pizza"));
		parent.add(new DefaultMutableTreeNode("ravioli"));
		parent.add(new DefaultMutableTreeNode("bananas"));

		treeModel = new DefaultTreeModel(root);
	}

	/**
	 * Test roots and children.
	 */
	@Test
	public void rootsAndChildren()
	{
		TreeModelProvider<DefaultMutableTreeNode> provider = new TreeModelProvider<DefaultMutableTreeNode>(
			treeModel)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<DefaultMutableTreeNode> model(DefaultMutableTreeNode object)
			{
				return Model.of(object);
			}
		};

		Iterator<DefaultMutableTreeNode> roots = provider.getRoots();
		assertTrue(roots.hasNext());
		DefaultMutableTreeNode root = roots.next();
		assertEquals("JTree", root.getUserObject());
		assertFalse(roots.hasNext());

		Iterator<DefaultMutableTreeNode> children = provider.getChildren(root);
		assertTrue(children.hasNext());
		assertEquals("colors", children.next().getUserObject());
		assertTrue(children.hasNext());
		assertEquals("sports", children.next().getUserObject());
		assertTrue(children.hasNext());
		assertEquals("food", children.next().getUserObject());
		assertFalse(roots.hasNext());

		treeModel.nodeChanged(root);
	}

	/**
	 * Test updating.
	 */
	@Test
	public void update()
	{
		TreeModelProvider<DefaultMutableTreeNode> provider = new TreeModelProvider<DefaultMutableTreeNode>(
			treeModel)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IModel<DefaultMutableTreeNode> model(DefaultMutableTreeNode object)
			{
				return Model.of(object);
			}
		};

		assertFalse(provider.completeUpdate);
		assertEquals(null, provider.nodeUpdates);
		assertEquals(null, provider.branchUpdates);

		treeModel.removeNodeFromParent((MutableTreeNode)root.getChildAt(0).getChildAt(0));

		assertFalse(provider.completeUpdate);
		assertEquals(null, provider.nodeUpdates);
		assertEquals(1, provider.branchUpdates.size());

		treeModel.nodeChanged(root.getChildAt(1));

		assertFalse(provider.completeUpdate);
		assertEquals(1, provider.nodeUpdates.size());
		assertEquals(1, provider.branchUpdates.size());

		treeModel.nodeStructureChanged(root.getChildAt(2));

		assertFalse(provider.completeUpdate);
		assertEquals(1, provider.nodeUpdates.size());
		assertEquals(2, provider.branchUpdates.size());

		treeModel.setRoot(new DefaultMutableTreeNode("bam!"));

		assertTrue(provider.completeUpdate);
		assertEquals(1, provider.nodeUpdates.size());
		assertEquals(2, provider.branchUpdates.size());
	}
}
