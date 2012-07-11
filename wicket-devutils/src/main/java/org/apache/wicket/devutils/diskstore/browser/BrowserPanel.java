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
package org.apache.wicket.devutils.diskstore.browser;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

/**
 * A panel that shows the data about pages in the data store
 */
public class BrowserPanel extends Panel
{

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public BrowserPanel(String id)
	{
		super(id);

		final DropDownChoice<String> sessionsSelector = createSessionsSelector("sessions");
		add(sessionsSelector);

		final BrowserTable table = createTable("table", sessionsSelector.getModel());
		add(table);

		AjaxFallbackLink<Void> refreshLink = new AjaxFallbackLink<Void>("refresh")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (target != null)
				{
					target.add(table);
				}
			}
		};
		add(refreshLink);

		AjaxFallbackLink<Void> currentSessionLink = new AjaxFallbackLink<Void>("currentSessionLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				sessionsSelector.setModelObject(getCurrentSession().getObject());
				if (target != null)
				{
					target.add(sessionsSelector, table);
				}
			}

			@Override
			public boolean isVisible()
			{
				return BrowserPanel.this.getSession().isTemporary() == false;
			}
		};
		currentSessionLink.setOutputMarkupPlaceholderTag(true);
		add(currentSessionLink);

		sessionsSelector.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(table);
			}
		});
	}

	private DropDownChoice<String> createSessionsSelector(String id)
	{
		IModel<String> defaultSession = getCurrentSession();

		DropDownChoice<String> sessionsSelector = new DropDownChoice<String>("sessions",
			defaultSession, new SessionsProviderModel());


		return sessionsSelector;
	}

	private IModel<String> getCurrentSession()
	{
		return Model.of(getSession().getId());
	}

	private BrowserTable createTable(String id, IModel<String> sessionId)
	{
		PageWindowProvider provider = new PageWindowProvider(sessionId);

		List<IColumn<PageWindowDescription, String>> columns = new ArrayList<IColumn<PageWindowDescription, String>>();

		PageWindowColumn pageIdColumn = new PageWindowColumn(Model.of("Id"), "id");
		columns.add(pageIdColumn);

		PageWindowColumn pageNameColumn = new PageWindowColumn(Model.of("Name"), "name");
		columns.add(pageNameColumn);

		PageWindowColumn pageSizeColumn = new PageWindowColumn(Model.of("Size"), "size");
		columns.add(pageSizeColumn);

		BrowserTable browserTable = new BrowserTable(id, columns, provider);
		browserTable.setOutputMarkupId(true);

		browserTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));

		return browserTable;
	}

}
