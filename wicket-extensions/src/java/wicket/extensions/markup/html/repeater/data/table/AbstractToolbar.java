package wicket.extensions.markup.html.repeater.data.table;

import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * A base class for data table toolbars
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class AbstractToolbar extends Panel
{
	private static final long serialVersionUID = 1L;

	private DataTable table;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            model
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AbstractToolbar(IModel model, DataTable table)
	{
		super(DataTable.TOOLBAR_COMPONENT_ID, model);
		this.table = table;
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	public AbstractToolbar(DataTable table)
	{
		super(DataTable.TOOLBAR_COMPONENT_ID);
		this.table = table;
	}

	/**
	 * @return DataTable this toolbar is attached to
	 */
	protected DataTable getTable()
	{
		return table;
	}
}
