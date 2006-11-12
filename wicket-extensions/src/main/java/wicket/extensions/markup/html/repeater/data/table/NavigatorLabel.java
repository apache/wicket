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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.MarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * Label that provides Showing x to y of z message given for a DataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class NavigatorLabel extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param table
	 *            dataview
	 */
	public NavigatorLabel(MarkupContainer parent, final String id, final DataTable table)
	{
		super(parent, id, new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				int of = table.getRowCount();
				int from = table.getCurrentPage() * table.getRowsPerPage();
				int to = Math.min(of, from + table.getRowsPerPage());

				from++;

				if (of == 0)
				{
					from = 0;
					to = 0;
				}

				return new String("Showing " + from + " to " + to + " of " + of);
			}
		});
	}
}
