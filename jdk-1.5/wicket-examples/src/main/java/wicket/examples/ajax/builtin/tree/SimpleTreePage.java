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
package wicket.examples.ajax.builtin.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import wicket.extensions.markup.html.tree.AbstractTree;
import wicket.extensions.markup.html.tree.Tree;

/**
 * Page that shuws a simple tree (not a table).
 *  
 * @author Matej
 *
 */
public class SimpleTreePage extends BaseTreePage
{
	private Tree tree;

	protected AbstractTree getTree()
	{
		return tree;
	}
	
	/**
	 * Page constructor
	 *
	 */
	public SimpleTreePage()
	{
		tree = new Tree("tree", createTreeModel()) 
		{
			protected String renderNode(TreeNode node)
			{
				ModelBean bean = (ModelBean) ((DefaultMutableTreeNode)node).getUserObject();
				return bean.getProperty1();
			}
		};
		add(tree);		
		tree.getTreeState().collapseAll();
	}

}
