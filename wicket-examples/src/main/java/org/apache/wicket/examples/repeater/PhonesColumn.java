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
package org.apache.wicket.examples.repeater;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.IHideableColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IMovableColumn;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.ResourceDynamicColumn;
import org.apache.wicket.extensions.markup.html.clipboard.ClipboardCopyBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;

/**
 * Shows a contact's phone numbers. The template is read from {@code PhonesColumn.html}, or from
 * {@code PhonesColumn_de.html} for German, the way a panel's markup is.
 */
public class PhonesColumn extends ResourceDynamicColumn<Contact>
	implements
		IHeaderContributor,
		IHideableColumn,
		IMovableColumn
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param header
	 *            the header text
	 */
	public PhonesColumn(IModel<String> header)
	{
		super(header);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		ClipboardCopyBehavior.renderHeadItems(response);
	}
}
