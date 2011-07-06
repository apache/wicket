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
package org.apache.wicket.devutils.diskstore;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.pageStore.PageWindowManager.PageWindow;
import org.apache.wicket.serialize.ISerializer;

/**
 * A model which can be used to show the PageWindows in a {@link AbstractTree tree}
 */
// Currently not used
class PageWindowModel extends AbstractReadOnlyModel<DefaultTreeModel>
{
	private final DefaultTreeModel treeModel;

	public PageWindowModel(String sessionId, DebugDiskDataStore dataStore)
	{
		List<PageWindow> pageWindows = dataStore.getLastPageWindows(sessionId, 50);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		initialize(rootNode, pageWindows, dataStore, sessionId);
		treeModel = new DefaultTreeModel(rootNode);
	}

	@Override
	public DefaultTreeModel getObject()
	{
		return treeModel;
	}

	private void initialize(final DefaultMutableTreeNode root, final List<PageWindow> pageWindows,
		DebugDiskDataStore dataStore, String sessionId)
	{
		ISerializer serializer = Application.get().getFrameworkSettings().getSerializer();

		for (PageWindow pageWindow : pageWindows)
		{
			int pageId = pageWindow.getPageId();
			DefaultMutableTreeNode pageIdNode = new DefaultMutableTreeNode(pageId);
			root.add(pageIdNode);

			byte[] data = dataStore.getData(sessionId, pageId);
			Object page = serializer.deserialize(data);
			DefaultMutableTreeNode pageNameNode = new DefaultMutableTreeNode(page.getClass()
				.getName());
			pageIdNode.add(pageNameNode);

			DefaultMutableTreeNode pageSizeNode = new DefaultMutableTreeNode("Size: " +
				data.length + " bytes");
			pageIdNode.add(pageSizeNode);
		}
	}
}
