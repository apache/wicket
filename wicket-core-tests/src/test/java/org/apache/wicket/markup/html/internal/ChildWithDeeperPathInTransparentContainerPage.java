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
package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

/**
 * Test page for https://issues.apache.org/jira/browse/WICKET-4172
 */
public class ChildWithDeeperPathInTransparentContainerPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	static final String LABEL_TEXT = "Column Info text";

	/**
	 * Constructor.
	 * 
	 * @param childVisible
	 */
	public ChildWithDeeperPathInTransparentContainerPage(final boolean childVisible)
	{
		add(new Label("version", getApplication().getFrameworkSettings().getVersion()));

		final TransparentWebMarkupContainer table_wrapper = new TransparentWebMarkupContainer(
			"table_wrapper");
		// additional stuff goes here
		add(table_wrapper);

		final WebMarkupContainer column = new WebMarkupContainer("column");
		// addition stuff goes here
		add(column);

		column.add(new Label("info", LABEL_TEXT)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible()
			{
				return childVisible;
			}
		});
		// just for test (returns a list of real items in our project)
		add(new Label("list", new Model<String>("Test Items")));

	}
}
