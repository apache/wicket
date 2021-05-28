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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.toggle.IToggleable;
import org.apache.wicket.markup.html.GenericWebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * A data table builds on data grid view to introduce toolbars. Toolbars can be used to display
 * sortable column headers, paging information, filter controls, and other information.
 * <p>
 * Data table also provides its own markup for an html table so the developer does not need to provide it
 * himself. This makes it very simple to add a datatable to the markup, however, some flexibility.
 * <p>
 * Example
 *
 * <pre>
 * &lt;table wicket:id=&quot;datatable&quot;&gt;&lt;/table&gt;
 * </pre>
 * <p>
 * And the related Java code: ( the second column will be sortable because its sort property is
 * specified, the third column will not )
 *
 * <pre>
 * // Application specific POJO to view/edit
 * public class MyEntity {
 *   private String firstName;
 *   private String lastName;
 *
 *   // getters and setters
 * }
 *
 * public class MyEntityProvider implements IToggleableDataProvider&lt;MyEntity&gt; {
 *     ...
 * }
 *
 * List&lt;IColumn&lt;MyEntity, String&gt;&gt; columns = new ArrayList&lt;&gt;();
 *
 * columns.add(new ToggleColumn&lt;MyEntity, String&gt;(new Model&lt;String&gt;(&quot;Toggle&quot;)));
 * columns.add(new PropertyColumn&lt;MyEntity, String&gt;(new Model&lt;String&gt;(&quot;First Name&quot;), &quot;firstName&quot;, &quot;firstName&quot;));
 * columns.add(new PropertyColumn&lt;MyEntity, String&gt;(new Model&lt;String&gt;(&quot;Last Name&quot;), &quot;lastName&quot;));
 *
 * DataTable&lt;MyEntity,String&gt; table = new ToggleableDataTable&lt;&gt;(&quot;datatable&quot;, columns, new MyEntityProvider(), 10);
 * table.addBottomToolbar(new NavigationToolbar(table));
 * table.addTopToolbar(new HeadersToolbar(table, null));
 * add(table);
 * </pre>
 *
 * @param <T> The model object type
 * @param <S> the type of the sorting parameter
 * @author Roland Kurucz
 * @see DataTable
 */
public class ToggleableDataTable<T, S> extends DataTable<T, S> implements IToggleable<T> {

    private static final long serialVersionUID = 1L;

    private final Set<T> expandedRows = new HashSet<>(0);
    private final IToggleableDataProvider<T> toggleableDataProvider;

    /**
     * Constructor
     *
     * @param id           component id
     * @param columns      list of IColumn objects
     * @param dataProvider imodel for data provider
     * @param rowsPerPage  number of rows per page
     */
    public ToggleableDataTable(final String id, final List<? extends IColumn<T, S>> columns,
                               final IToggleableDataProvider<T> dataProvider, final long rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        this.toggleableDataProvider = dataProvider;
        columns
                .stream()
                .filter(column -> column instanceof ToggleColumn)
                .map(column -> (ToggleColumn<T, S>) column)
                .forEach(toggleColumn -> toggleColumn.setDataTable(this));
    }

    /**
     * Factory method for the DataGridView
     *
     * @param id           The component id
     * @param columns      list of IColumn objects
     * @param dataProvider imodel for data provider
     * @return the data grid view
     */
    @Override
    protected DataGridView<T> newDataGridView(final String id, final List<? extends IColumn<T, S>> columns, final IDataProvider<T> dataProvider) {
        return new ToggleableDataGridView<>(id, columns, dataProvider, this);
    }

    @Override
    public void toggle(final T object) {
        if (isCollapsible(object)) {
            collapse(object);
        } else if (isExpandable(object)) {
            expand(object);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void collapse(final T object) {
        expandedRows.remove(object);
    }

    private void expand(final T object) {
        expandedRows.add(object);
    }

    @Override
    public boolean isCollapsible(final T object) {
        return toggleableDataProvider.isToggleable(object) && expandedRows.contains(object);
    }

    @Override
    public boolean isExpandable(final T object) {
        return toggleableDataProvider.isToggleable(object) && !expandedRows.contains(object);
    }

    protected Item<T> newExpansionRowItem(final String id, final int index, final IModel<T> model) {
        return newRowItem(id, index, model);
    }

    protected Component newExpansionRowContent(final String id, final IModel<T> model) {
        return new Label(id, model);
    }

    private static class ToggleableDataGridView<T, S> extends DataGridView<T> {
        private final ToggleableDataTable<T, S> toggleableDataTable;

        public ToggleableDataGridView(final String id, final List<? extends ICellPopulator<T>> iCellPopulators, final IDataProvider<T> dataProvider, final ToggleableDataTable<T, S> toggleableDataTable) {
            super(id, iCellPopulators, dataProvider);
            this.toggleableDataTable = toggleableDataTable;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected Item<ICellPopulator<T>> newCellItem(final String id, final int index, final IModel model) {
            final Item<ICellPopulator<T>> item = toggleableDataTable.newCellItem(id, index, model);
            final IColumn<T, S> column = toggleableDataTable.getColumns().get(index);
            Optional.of(column)
                    .filter(c -> c instanceof IStyledColumn)
                    .map(c -> (IStyledColumn<T, S>) c)
                    .map(IStyledColumn::getCssClass)
                    .map(cssClass -> cssClass.split(" "))
                    .map(Set::of)
                    .ifPresent(cssClasses -> item.add(new ClassAttributeModifier() {
                        @Override
                        protected Set<String> update(Set<String> oldClasses) {
                            oldClasses.addAll(cssClasses);
                            return oldClasses;
                        }
                    }));
            return item;
        }

        @Override
        protected IItemFactory<T> newItemFactory() {
            return (index, model) -> {
                final Item<T> row = new Item<>(ToggleableDataGridView.this.newChildId(), index, model);
                final Item<T> toggleableRow = ToggleableDataGridView.this.newItem("toggleableRow", index, model);
                ToggleableDataGridView.this.populateItem(toggleableRow);
                row.add(toggleableRow);
                final Item<T> expansionRow = ToggleableDataGridView.this.newExpansionRow(index, model);
                row.add(expansionRow);
                return row;
            };
        }

        private Item<T> newExpansionRow(int index, IModel<T> model) {
            final Item<T> expansionRow = toggleableDataTable.newExpansionRowItem("expansionRow", index, model);
            final GenericWebMarkupContainer<T> cell = new GenericWebMarkupContainer<>("cell");
            cell.add(AttributeModifier.replace("colspan", new LoadableDetachableModel<Integer>() {
                @Override
                protected Integer load() {
                    return toggleableDataTable.getColumns().size();
                }
            }));
            final Component expansionRowContent = toggleableDataTable.newExpansionRowContent("expansion", model);
            cell.add(expansionRowContent);
            expansionRow.add(cell)
                    .add(new ClassAttributeModifier() {
                        @Override
                        protected Set<String> update(Set<String> oldClasses) {
                            oldClasses.add("expansion-row");
                            return oldClasses;
                        }

                        @Override
                        public void onConfigure(Component component) {
                            super.onConfigure(component);
                            component.setVisible(toggleableDataTable.isCollapsible(model.getObject()));
                        }
                    });
            return expansionRow;
        }

        @Override
        protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
            return toggleableDataTable.newRowItem(id, index, model);
        }
    }
}
