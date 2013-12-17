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
package org.apache.wicket.extensions.markup.html.repeater.tree.table;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Response;

/**
 * A border for a node component which renders nested <code>DIV</code>s to simulate the structure of
 * parental branches inside a tabular layout.
 * 
 * @see NodeModel
 * @see TreeColumn#populateItem(Item, String, IModel)
 * @author svenmeier
 */
public class NodeBorder extends Behavior
{

	private static final long serialVersionUID = 1L;

	private boolean[] branches;

	/**
	 * @param branches
	 *            branch
	 */
	public NodeBorder(boolean[] branches)
	{
		this.branches = branches;
	}

	@Override
	public void beforeRender(Component component)
	{
		Response response = component.getResponse();

		for (int i = 0; i < branches.length; i++)
		{
			if (i > 0)
			{
				response.write("<div class=\"tree-subtree\">");
			}

			if (branches[i])
			{
				response.write("<div class=\"tree-branch tree-branch-mid\">");
			}
			else
			{
				response.write("<div class=\"tree-branch tree-branch-last\">");
			}
		}
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		tag.append("class", "tree-node", " ");
	}

	@Override
	public void afterRender(Component component)
	{
		Response response = component.getResponse();

		for (int i = 0; i < branches.length; i++)
		{
			if (i > 0)
			{
				response.write("</div>");
			}
			response.write("</div>");
		}
	}
}
