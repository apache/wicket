package wicket.extensions.ajax.markup.html.repeater.data.sort;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import wicket.markup.html.WebMarkupContainer;

public class AjaxFallbackHeadersToolbar extends HeadersToolbar
{
	private static final long serialVersionUID = 1L;

	public AjaxFallbackHeadersToolbar(DataTable table, ISortStateLocator stateLocator)
	{
		super(table, stateLocator);
		table.setOutputMarkupId(true);
	}

	protected WebMarkupContainer newSortableHeader(String borderId, String property,
			ISortStateLocator locator)
	{
		return new AjaxFallbackOrderByBorder(borderId, property, locator, getAjaxCallDecorator())
		{
			private static final long serialVersionUID = 1L;

			protected void onAjaxClick(AjaxRequestTarget target)
			{
				target.addComponent(getTable());

			}

		};
	}

	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}


}
