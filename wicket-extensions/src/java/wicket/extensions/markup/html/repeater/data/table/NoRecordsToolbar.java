package wicket.extensions.markup.html.repeater.data.table;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A toolbar that displays a "no records found" message when the data table
 * contains no rows.
 * <p>
 * The message can be overridden by providing a resource with key
 * <code>datatable.no-records-found</code>
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NoRecordsToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	private static final IModel DEFAULT_MESSAGE_MODEL = new AbstractReadOnlyModel()
	{
		private static final long serialVersionUID = 1L;

		public Object getObject(Component component)
		{
			return component.getLocalizer().getString("datatable.no-records-found", component,
					"No Records Found");
		}
	};


	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public NoRecordsToolbar(final DataTable table)
	{
		this(table, DEFAULT_MESSAGE_MODEL);
	}

	/**
	 * @param table
	 *            data table this toolbar will be attached to
	 * @param messageModel
	 *            model that will be used to display the "no records found"
	 *            message
	 */
	public NoRecordsToolbar(final DataTable table, IModel messageModel)
	{
		super(table);
		WebMarkupContainer td = new WebMarkupContainer("td");
		add(td);

		td.add(new AttributeModifier("colspan", true, new Model(String
				.valueOf(table.getColumns().length))));
		td.add(new Label("msg", messageModel));
	}

	/**
	 * Only shows this toolbar when there are no rows
	 * 
	 * @see wicket.Component#isVisible()
	 */
	public boolean isVisible()
	{
		return getTable().getRowCount() == 0;
	}

}
