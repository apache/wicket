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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * A panel that renders &lt;colgroup&gt; with &lt;col&gt; elements
 * inside it.
 *
 * The columns can be used to style the whole table column.
 *
 * <p>
 * <strong>Important</strong>: this component requires
 * {@link org.apache.wicket.settings.MarkupSettings#getStripWicketTags()}
 * to return {@code true}, otherwise the browsers break the rendering of
 * the HTML elements. For example Google Chrome renders two &lt;colgroup&gt;
 * elements.
 * </p>
 *
 * @see DataTable#getColGroup()
 */
public class ColGroup extends Panel
{
	private static final long serialVersionUID = 1L;

	private final RepeatingView colgroupCols;

	public ColGroup(String id)
	{
		super(id);

		this.colgroupCols = new RepeatingView("col");
		add(colgroupCols);
	}

	/**
	 * Adds a column to the group.
	 *
	 * <p>Usage:
	 * <pre>
	 *     <code>
	 *         colgroup.addCol(colgroup.new Col(AttributeModifier.append("span", "2"),
	 *             AttributeModifier.append("style", "background-color: #CC6633")))
	 *     </code>
	 * </pre>
	 *
	 * </p>
	 *
	 * @param column
	 *          The column with the styling behaviors
	 * @return {@code this}, for method chaining
	 */
	public ColGroup addCol(Col column)
	{
		colgroupCols.add(column);
		return this;
	}

	/**
	 * Hides the ColGroup if there are no &lt;col&gt;s to render
	 */
	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		setVisible(colgroupCols.size() > 0);
	}

	public class Col extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		public Col(Behavior... behaviors)
		{
			super(colgroupCols.newChildId());

			if (behaviors != null)
			{
				add(behaviors);
			}
		}
	}
}
