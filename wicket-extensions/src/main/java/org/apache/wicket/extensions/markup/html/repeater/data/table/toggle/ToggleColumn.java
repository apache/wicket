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
package org.apache.wicket.extensions.markup.html.repeater.data.table.toggle;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.toggle.IToggleable;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.Set;

/**
 * A column displaying the toggleable rows.
 *
 * @param <T> The model object type
 * @param <S> the type of the sorting parameter
 * @author Roland Kurucz
 */
public class ToggleColumn<T, S> extends AbstractColumn<T, S> {

    private IToggleable<T> dataTable;

    public ToggleColumn(final IModel<String> displayModel) {
        super(displayModel);
    }

    @Override
    public void populateItem(final Item<ICellPopulator<T>> cellItem, final String componentId, final IModel<T> rowModel) {
        final Component toggleComponent = newToggleComponent(componentId, rowModel);
        toggleComponent.add(new ClassAttributeModifier() {
            @Override
            protected Set<String> update(Set<String> oldClasses) {
                final T object = rowModel.getObject();
                if (dataTable.isToggleable(object)) {
                    oldClasses.add("toggleable");
                    if (dataTable.isCollapsible(object)) {
                        oldClasses.add("collapsible");
                    } else if (dataTable.isExpandable(object)) {
                        oldClasses.add("expandable");
                    }
                }
                return oldClasses;
            }

            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                final T object = rowModel.getObject();
                component.setVisible(dataTable.isToggleable(object));
            }
        });
        cellItem.add(toggleComponent);
    }

    protected Component newToggleComponent(final String id, final IModel<T> rowModel) {
        return new Link<>(id, rowModel) {
            @Override
            public void onClick() {
                ToggleColumn.this.toggle(rowModel.getObject());
            }
        }.setBody(Model.of(""));
    }

    final <T1 extends DataTable<T, S> & IToggleable<T>> void setDataTable(final T1 dataTable) {
        this.dataTable = dataTable;
    }

    protected final void toggle(final T object) {
        dataTable.toggle(object);
    }
}
