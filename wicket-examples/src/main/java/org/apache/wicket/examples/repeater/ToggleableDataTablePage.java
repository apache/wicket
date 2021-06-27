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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.toggle.ToggleColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.toggle.ToggleableDataTable;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import java.util.ArrayList;
import java.util.List;


/**
 * demo page for the toggleable datatable component
 *
 * @author Roland Kurucz
 * @see ToggleableDataTablePage
 */
public class ToggleableDataTablePage extends BasePage {

    private static final String TOGGLE = "toggle";
    private static final String ACTIONS = "actions";
    private static final String ID = "id";
    private static final String FIRSTNAME = "firstName";
    private static final String LASTNAME = "lastName";
    private static final String HOME_PHONE = "homePhone";
    private static final String CELL_PHONE = "cellPhone";
    private static final String BORN_DATE = "bornDate";

    /**
     * constructor
     */
    public ToggleableDataTablePage() {
        add(createToggleableDataTable());
        add(createAjaxToggleableDataTable());
    }

    private ToggleableDataTable<Contact, String> createToggleableDataTable() {

        List<IColumn<Contact, String>> columns = new ArrayList<>();

        columns.add(new ToggleColumn<>(new ResourceModel(TOGGLE)));
        columns.add(new AbstractColumn<>(new ResourceModel(ACTIONS)) {
            @Override
            public void populateItem(Item<ICellPopulator<Contact>> cellItem, String componentId, IModel<Contact> model) {
                cellItem.add(new ActionPanel(componentId, model));
            }
        });
        columns.add(new LambdaColumn<>(new ResourceModel(ID), Contact::getId) {
            @Override
            public String getCssClass() {
                return "numeric";
            }
        });

        columns.add(new LambdaColumn<>(new ResourceModel(FIRSTNAME), FIRSTNAME, Contact::getFirstName));

        columns.add(new LambdaColumn<>(new ResourceModel(LASTNAME), LASTNAME, Contact::getLastName) {
            @Override
            public String getCssClass() {
                return "last-name";
            }
        });

        columns.add(new LambdaColumn<>(new ResourceModel(HOME_PHONE), Contact::getHomePhone));
        columns.add(new LambdaColumn<>(new ResourceModel(CELL_PHONE), Contact::getCellPhone));
        columns.add(new LambdaColumn<>(new ResourceModel(BORN_DATE), Contact::getBornDate));

        ToggleableContactDataProvider dataProvider = new ToggleableContactDataProvider();
        ToggleableDataTable<Contact, String> dataTable = new ToggleableDataTable<>("table", columns,
                dataProvider, 8);
        dataTable.addTopToolbar(new HeadersToolbar<>(dataTable, dataProvider));
        dataTable.addBottomToolbar(new NavigationToolbar(dataTable));
        return dataTable;
    }

    private ToggleableDataTable<Contact, String> createAjaxToggleableDataTable() {

        List<IColumn<Contact, String>> columns = new ArrayList<>();

        columns.add(new ToggleColumn<>(new ResourceModel(TOGGLE)) {
            @Override
            protected Component newToggleComponent(String id, IModel<Contact> rowModel) {
                return new AjaxLink<>(id, rowModel) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        toggle(rowModel.getObject());
                        target.add(findParent(ToggleableDataTable.class));
                    }
                }.setBody(Model.of(""));
            }
        });
        columns.add(new AbstractColumn<>(new ResourceModel(ACTIONS)) {
            @Override
            public void populateItem(Item<ICellPopulator<Contact>> cellItem, String componentId, IModel<Contact> model) {
                cellItem.add(new ActionPanel(componentId, model));
            }
        });
        columns.add(new LambdaColumn<>(new ResourceModel(ID), Contact::getId) {
            @Override
            public String getCssClass() {
                return "numeric";
            }
        });

        columns.add(new LambdaColumn<>(new ResourceModel(FIRSTNAME), FIRSTNAME, Contact::getFirstName));

        columns.add(new LambdaColumn<>(new ResourceModel(LASTNAME), LASTNAME, Contact::getLastName) {
            @Override
            public String getCssClass() {
                return "last-name";
            }
        });

        columns.add(new LambdaColumn<>(new ResourceModel(HOME_PHONE), Contact::getHomePhone));
        columns.add(new LambdaColumn<>(new ResourceModel(CELL_PHONE), Contact::getCellPhone));
        columns.add(new LambdaColumn<>(new ResourceModel(BORN_DATE), Contact::getBornDate));

        ToggleableContactDataProvider dataProvider = new ToggleableContactDataProvider();
        ToggleableDataTable<Contact, String> dataTable = new ToggleableDataTable<>("ajaxTable", columns,
                dataProvider, 8) {
            @Override
            protected Item<Contact> newRowItem(String id, int index, IModel<Contact> model) {
                return new OddEvenItem<>(id, index, model);
            }

            @Override
            protected Component newExpansionRowContent(String id, IModel<Contact> model) {
                return new MultiLineLabel(id, new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        final Contact contact = model.getObject();
                        return "This is the expanded content of the current row! \n" +
                                "Contact Name: " + contact.getFirstName() + " " + contact.getLastName();
                    }
                });
            }
        };
        dataTable.addTopToolbar(new AjaxFallbackHeadersToolbar<>(dataTable, dataProvider));
        dataTable.addBottomToolbar(new AjaxNavigationToolbar(dataTable));
        return dataTable;
    }
}
