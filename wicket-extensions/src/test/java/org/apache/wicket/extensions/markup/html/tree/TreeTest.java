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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class TreeTest extends WicketTestCase
{

	/**
	 * Asserting that {@link AbstractTree#treeNodesInserted(javax.swing.event.TreeModelEvent)} adds
	 * the new item to the dirtyItemsCreateDOM, since there is no parent node at client to be
	 * recreated.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3309">WICKET-3309</a>
	 */
	@Test
	public void addChildOnRootAtAnOnRootLessTree()
	{
		TestPage testPage = new TestPage();
		testPage.tree.setRootLess(true);
		tester.startPage(testPage);
		tester.clickLink("addToRoot", true);
		assertTrue(tester.getLastResponseAsString().contains("rootChild"));
	}

	/**
	 * Asserting that {@link AbstractTree#treeNodesInserted(javax.swing.event.TreeModelEvent)} don't
	 * add and not presented node to the AJAX response by invalidating it.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3309">WICKET-3309</a>
	 */
	@Test
	public void addGrandchildOnRootAtAnRootLessTree()
	{
		TestPage testPage = new TestPage();
		testPage.tree.setRootLess(true);
		DefaultMutableTreeNode rootChild = new DefaultMutableTreeNode("rootChild");
		testPage.rootNode.add(rootChild);
		testPage.tree.getTreeState().selectNode(rootChild, true);
		tester.startPage(testPage);
		tester.clickLink("addChildToSelected", true);
		assertTrue(tester.getLastResponseAsString().contains("newNode"));
	}

	/**
	 * Asserting the old leaf root node gets a junction link when adding its first child
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3449">WICKET-3449</a>
	 */
	@Test
	public void junctionLinkRendered()
	{
		TestPage testPage = new TestPage();
		tester.startPage(testPage);
		tester.clickLink("addToRoot", true);
		assertTrue(tester.getLastResponseAsString().contains("junctionLink"));
	}

	/** */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		AbstractTree tree;
		DefaultTreeModel treeModel;
		DefaultMutableTreeNode rootNode;

		/** */
		public TestPage()
		{
			rootNode = new DefaultMutableTreeNode("ROOT");
			treeModel = new DefaultTreeModel(rootNode);
			tree = new LinkTree("tree", treeModel);
			add(tree);
			add(new AjaxLink<Void>("addToRoot")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					DefaultMutableTreeNode child = new DefaultMutableTreeNode("rootChild");
					treeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
					tree.updateTree(target);
				}
			});
			add(new AjaxLink<Void>("addChildToSelected")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getTreeState()
						.getSelectedNodes()
						.iterator()
						.next();
					treeModel.insertNodeInto(new DefaultMutableTreeNode("newNode"), selectedNode,
						selectedNode.getChildCount());
					tree.updateTree(target);
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" + "<div wicket:id=\"tree\"></div>"
				+ "<a wicket:id=\"addToRoot\"></a><a wicket:id=\"addChildToSelected\"></a>"
				+ "</body></html>");
		}

	}
}
