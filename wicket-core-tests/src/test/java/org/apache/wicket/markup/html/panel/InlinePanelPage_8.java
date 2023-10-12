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
package org.apache.wicket.markup.html.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * 
 */
public class InlinePanelPage_8 extends WebPage
{
	private static final long serialVersionUID = 1L;
	private int number = 0;
	private final List<ListNode> nodes = new ArrayList<ListNode>();

	/**
	 * Construct.
	 */
	public InlinePanelPage_8()
	{
		ListNode first = new ListNode("first", number++);
		nodes.add(first);
		add(first);
		add(new AjaxLink<Void>("add")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ListNode last = nodes.get(nodes.size() - 1);
				ListNode newLast = last.addNext(target, number++);
				nodes.add(newLast);
			}
		});
	}

	/**
	 * 
	 */
	public class ListNode extends Fragment
	{
		private static final long serialVersionUID = 1L;

		private final WebMarkupContainer nextContainer;

		/**
		 * Construct.
		 * 
		 * @param id
		 * @param number
		 */
        ListNode(String id, int number)
		{
			super(id, "node", InlinePanelPage_8.this);
			add(new Label("number", Integer.toString(number)));
			nextContainer = new WebMarkupContainer("nextContainer");
			nextContainer.setOutputMarkupPlaceholderTag(true);
			nextContainer.setVisible(false);
			add(nextContainer);
		}

		/**
		 * 
		 * @param target
		 * @param number
		 * @return the added {@link ListNode}
		 */
        ListNode addNext(AjaxRequestTarget target, int number)
		{
			ListNode next = new ListNode("next", number);
			nextContainer.add(next);
			nextContainer.setVisible(true);
			target.add(nextContainer);
			return next;
		}
	}
}
