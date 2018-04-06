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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ColGroup;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.CSVDataExporter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.export.ExportToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.DateConverter;


/**
 * demo page for the datatable component
 * 
 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
 * @author igor
 * 
 */
public class DataTablePage extends BasePage
{
	/**
	 * constructor
	 */
	public DataTablePage()
	{
		List<IColumn<Contact, String>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<Contact, String>(new ResourceModel("actions"))
		{
			@Override
			public void populateItem(Item<ICellPopulator<Contact>> cellItem, String componentId,
				IModel<Contact> model)
			{
				cellItem.add(new ActionPanel(componentId, model));
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new ResourceModel("id"), "id")
		{
			@Override
			public String getCssClass()
			{
				return "numeric";
			}
		});

		columns.add(new LambdaColumn<>(new ResourceModel("firstName"), "firstName", Contact::getFirstName));

		columns.add(new LambdaColumn<Contact, String>(new ResourceModel("lastName"), "lastName", Contact::getLastName)
		{
			@Override
			public String getCssClass()
			{
				return "last-name";
			}
		});

		columns.add(new PropertyColumn<>(new ResourceModel("homePhone"), "homePhone"));
		columns.add(new PropertyColumn<>(new ResourceModel("cellPhone"), "cellPhone"));
		columns.add(new PropertyColumn<>(new ResourceModel("bornDate"), "bornDate"));

		SortableContactDataProvider dataProvider = new SortableContactDataProvider();
		DataTable<Contact, String> dataTable = new DefaultDataTable<>("table", columns,
				dataProvider, 8);
		
		dataTable.addBottomToolbar(new ExportToolbar(dataTable).addDataExporter(new CSVDataExporter() {
			/**
			 * Use converters of this page.
			 * 
			 * @see DataTablePage#createConverter(Class)
			 */
			@Override
			protected IConverterLocator getConverterLocator()
			{
				return DataTablePage.this;
			}
			
			/**
			 * Wrap models on the page so resource properties get picked up. 
			 */
			@Override
			protected <T> IModel<T> wrapModel(IModel<T> model)
			{
				return DataTablePage.this.wrap(model);
			}
		}));

		add(dataTable);

		DataTable<Contact, String> tableWithColGroup = new DataTable<>("tableWithColGroup", columns,
				dataProvider, 8);
		tableWithColGroup.addTopToolbar(new HeadersToolbar<>(tableWithColGroup, dataProvider));
		add(tableWithColGroup);
		
		//This is a table that uses ColGroup to style the columns: 
		ColGroup colgroup = tableWithColGroup.getColGroup();
		colgroup.add(AttributeModifier.append("style", "border: solid 1px green;"));
		colgroup.addCol(colgroup.new Col(AttributeModifier.append("style", "background-color: lightblue;")));
		colgroup.addCol(colgroup.new Col(AttributeModifier.append("style", "background-color: lightgreen")));
		colgroup.addCol(colgroup.new Col(AttributeModifier.append("style", "background-color: pink")));
		colgroup.addCol(colgroup.new Col(AttributeModifier.append("style", "background-color: yellow")));
		colgroup.addCol(colgroup.new Col(AttributeModifier.append("span", "2"),
				AttributeModifier.append("style", "background-color: #CC6633")));
	}
	
	/**
	 * Custom format for date in export.
	 */
	@Override
	protected IConverter<?> createConverter(Class<?> type)
	{
		if (type == Date.class) {
			return new DateConverter() {
				@Override
				public DateFormat getDateFormat(Locale locale)
				{
					return (DateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale).clone();
				}
			};
		}
		return null;
	}
}
