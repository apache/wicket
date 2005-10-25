package wicket.extensions.markup.html.repeater.data.table;


import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;


/**
 * Label that provides Showing x to y of z message given a dataview.
 * 
 * @author igor
 * 
 */
public class NavigatorLabel extends Label
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 * @param dataView
	 *            dataview
	 */
	public NavigatorLabel(final String id, final DataView dataView)
	{
		super(id, new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			public Object getObject(Component component)
			{
				int of = dataView.getItemCount();
				int from = dataView.getCurrentPage() * dataView.getItemsPerPage();
				int to = Math.min(of, from + dataView.getItemsPerPage());

				from++;

				if (of == 0)
				{
					from = 0;
					to = 0;
				}

				return new String("Showing " + from + " to " + to + " of " + of);
			}
		});
	}
}
