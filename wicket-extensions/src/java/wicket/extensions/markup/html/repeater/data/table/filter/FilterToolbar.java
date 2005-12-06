package wicket.extensions.markup.html.repeater.data.table.filter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
import wicket.extensions.markup.html.repeater.data.table.AbstractDataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.extensions.markup.html.repeater.data.table.Toolbar;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.Form;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;

public class FilterToolbar extends Toolbar
{
	private static final long serialVersionUID = 1L;
	
	public FilterToolbar(final AbstractDataTable table, final IFilterStateLocator stateLocator) {
		super(AbstractDataTable.TOOLBAR_COMPONENT_ID);
		
		//TODO if statelocator==null throw illegalarg
		
		Form form=new Form("filterForm", new Model((Serializable)stateLocator.getFilterState()));
		//TODO versioning of the filter state
		add(form);
		
		//TODO reset page on change
		OrderedRepeatingView filters=new OrderedRepeatingView("filters");
		form.add(filters);
		
		IColumn[] cols=table.getColumns();
		for (int i=0;i<cols.length;i++) {
			WebMarkupContainer item=new WebMarkupContainer(filters.newChildId());
			item.setRenderBodyOnly(true);
			
			IColumn col=cols[i];
			if (col instanceof IFilteredColumn) {
				IFilteredColumn filteredCol=(IFilteredColumn)col;
				item.add(filteredCol.getFilter("filter", form));
			} else {
				item.add(new NoFilter("filter"));
			}
			
			filters.add(item);
		}
		
		
		
		
		/*		Form form=new Form("filtersForm");
		add(form);
		form.add(new ListView("filters", table.getColumns()) // <=== change to refreshing view or non-refresshing
		{
			private static final long serialVersionUID = 1L;

			protected void populateItem(ListItem item)
			{
				final IColumn column = (IColumn)item.getModelObject();
				if (column instanceof IFilteredColumn) {
					IFilteredColumn filterColumn=(IFilteredColumn)column;
					item.add(filterColumn.getFilter("filter"));
				} else {
					item.add(new NoFilter("filter"));
				}
			}

		});
	*/	

	}

}
