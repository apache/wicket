package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
import wicket.extensions.markup.html.repeater.data.table.AbstractDataTable;
import wicket.extensions.markup.html.repeater.data.table.IColumn;
import wicket.extensions.markup.html.repeater.data.table.Toolbar;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.HiddenField;
import wicket.model.Model;

/**
 * Toolbar that creates form components used to filter data in the data table it
 * is attached to.
 * 
 * @author Igor Vaynberg (ivaynber)
 * 
 */
public class FilterToolbar extends Toolbar
{
	private static final long serialVersionUID = 1L;
	  
	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be added to
	 * @param stateLocator
	 *            locator responsible for finding object used to store filter's
	 *            state
	 */
	public FilterToolbar(final AbstractDataTable table, final IFilterStateLocator stateLocator)
	{
		super(AbstractDataTable.TOOLBAR_COMPONENT_ID);

		if (table == null)
		{
			throw new IllegalArgumentException("argument [table] cannot be null");
		}
		if (stateLocator == null)
		{
			throw new IllegalArgumentException("argument [stateLocator] cannot be null");
		}

		// create the form used to contain all filter components

		final FilterForm form = new FilterForm("filterForm", stateLocator)
		{
			private static final long serialVersionUID = 1L;

			protected void onSubmit()
			{
				table.setCurrentPage(0);
			}
		};
		add(form);
		
		
		add(new WebMarkupContainer("focus-restore") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
			{
				String script="<script>_filter_focus_restore('"+form.getHiddenInputCssId()+"');</script>";
				replaceComponentTagBody(markupStream, openTag, script);
			}
		});

		// populate the toolbar with components provided by columns

		OrderedRepeatingView filters = new OrderedRepeatingView("filters");
		form.add(filters);

		IColumn[] cols = table.getColumns();
		for (int i = 0; i < cols.length; i++)
		{
			WebMarkupContainer item = new WebMarkupContainer(filters.newChildId());
			item.setRenderBodyOnly(true);

			IColumn col = cols[i];
			if (col instanceof IFilteredColumn)
			{
				IFilteredColumn filteredCol = (IFilteredColumn)col;
				item.add(filteredCol.getFilter("filter", form));
			}
			else
			{
				item.add(new NoFilter("filter"));
			}

			filters.add(item);
		}

	}
	
	

}
