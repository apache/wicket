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


import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;


/**
 * Label that provides Showing x to y of z message given for a DataTable. The message can be
 * overridden using the <code>NavigatorLabel</code> property key, the default message is used is of
 * the format <code>Showing ${from} to ${to} of ${of}</code>. The message can also be configured
 * pragmatically by setting it as the model object of the label.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NavigatorLabel extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param pageable
	 */
	public NavigatorLabel(final String id, final IPageableItems pageable)
	{
		super(id);
		setDefaultModel(new StringResourceModel("NavigatorLabel", this,
			new Model<LabelModelObject>(new LabelModelObject(pageable))));
	}

	private static class LabelModelObject implements IClusterable
	{
		private static final long serialVersionUID = 1L;
		private final IPageableItems pageable;

		/**
		 * Construct.
		 * 
		 * @param table
		 */
		public LabelModelObject(final IPageableItems table)
		{
			pageable = table;
		}

		/**
		 * @return "z" in "Showing x to y of z"
		 */
		public long getOf()
		{
			return pageable.getItemCount();
		}

		/**
		 * @return "x" in "Showing x to y of z"
		 */
		public long getFrom()
		{
			if (getOf() == 0)
			{
				return 0;
			}
			return pageable.getCurrentPage() * pageable.getItemsPerPage() + 1;
		}

		/**
		 * @return "y" in "Showing x to y of z"
		 */
		public long getTo()
		{
			if (getOf() == 0)
			{
				return 0;
			}
			return Math.min(getOf(), getFrom() + pageable.getItemsPerPage() - 1);
		}
	}
}
