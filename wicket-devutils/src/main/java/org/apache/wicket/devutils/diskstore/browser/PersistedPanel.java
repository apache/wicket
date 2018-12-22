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
import java.util.Optional;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.pageStore.DefaultPageContext;
import org.apache.wicket.pageStore.IPageContext;
import org.apache.wicket.pageStore.IPersistedPage;
import org.apache.wicket.pageStore.IPersistentPageStore;
import org.apache.wicket.util.time.Duration;

/**
 * A panel that shows data about {@link IPersistedPage}s in an {@link IPersistentPageStore}.
 */
public class PersistedPanel extends GenericPanel<IPersistentPageStore>
{

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public PersistedPanel(String id, IModel<IPersistentPageStore> store)
	{
		super(id, store);

		final Label storeLabel = new Label("store", () -> {
			IPersistentPageStore s = getModelObject();
			
			if (s == null) {
				return "N/A";
			}
			
			return String.format("%s - %s", s.getClass().getName(), s.getTotalSize());
		});
		storeLabel.setOutputMarkupId(true);
		add(storeLabel);
		
		final DropDownChoice<String> sessionsSelector = createSessionsSelector("sessions");
		sessionsSelector.setOutputMarkupId(true);
		add(sessionsSelector);

		final DataTable<IPersistedPage, String> table = createTable("table", sessionsSelector.getModel());
		table.setOutputMarkupId(true);
		add(table);

		AjaxFallbackLink<Void> refreshLink = new AjaxFallbackLink<Void>("refresh")
		{
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				targetOptional.ifPresent(target -> target.add(table));
			}
		};
		add(refreshLink);

		AjaxFallbackLink<Void> currentSessionLink = new AjaxFallbackLink<Void>("currentSessionLink")
		{
			@Override
			public void onClick(Optional<AjaxRequestTarget> targetOptional)
			{
				sessionsSelector.setModelObject(getCurrentSessionIdentifier());
				targetOptional.ifPresent(target -> target.add(sessionsSelector, table));
			}

			@Override
			public boolean isVisible()
			{
				return PersistedPanel.this.getSession().isTemporary() == false;
			}
		};
		currentSessionLink.setOutputMarkupPlaceholderTag(true);
		add(currentSessionLink);

		sessionsSelector.add(new AjaxFormComponentUpdatingBehavior("change")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				target.add(storeLabel);
				target.add(sessionsSelector);
				target.add(table);
			}
		});

		add(new AbstractAjaxTimerBehavior(Duration.seconds(5)) {

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				target.add(storeLabel);
				target.add(table);
			}
		});
	}

	private DropDownChoice<String> createSessionsSelector(String id)
	{
		DropDownChoice<String> sessionsSelector = new DropDownChoice<String>("sessions",
			Model.of(getCurrentSessionIdentifier()), new SessionIdentifiersModel(getModel()));

		return sessionsSelector;
	}

	private String getCurrentSessionIdentifier()
	{
		IPersistentPageStore store = getModelObject();
		if (store == null) {
			return null;
		}

		IPageContext context = new DefaultPageContext();
		
		return store.getSessionIdentifier(context);
	}

	private DataTable<IPersistedPage, String> createTable(String id, IModel<String> sessionId)
	{
		PersistedPagesProvider provider = new PersistedPagesProvider(sessionId, getModel());

		List<IColumn<IPersistedPage, String>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<IPersistedPage, String>(Model.of("Id"), "pageId")
		{
			@Override
			public void populateItem(Item<ICellPopulator<IPersistedPage>> cellItem, String componentId, IModel<IPersistedPage> rowModel)
			{
				cellItem.add(new Link<IPersistedPage>(componentId, rowModel)
				{
					@Override
					protected void onComponentTag(ComponentTag tag)
					{
						tag.setName("a");
						
						super.onComponentTag(tag);
					}
					
					@Override
					public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
					{
						replaceComponentTagBody(markupStream, openTag, "" + getModelObject().getPageId());
					}
					
					@Override
					public void onClick()
					{
						setResponsePage(new InspectorPage(new PageReference(getModelObject().getPageId())));
					}
				});
			}
		});
		columns.add(new PropertyColumn<>(Model.of("Type"), "pageType", "pageType"));
		columns.add(new PropertyColumn<>(Model.of("Size"), "pageSize", "pageSize"));

		DefaultDataTable<IPersistedPage, String> browserTable = new DefaultDataTable<>(id, columns, provider, 20);
		browserTable.setOutputMarkupId(true);

		return browserTable;
	}

}
