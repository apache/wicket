package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A helper implementation for a filtered column.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public abstract class FilteredAbstractColumn extends AbstractColumn implements IFilteredColumn
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to display the header text of this column
	 * 
	 * @param sortProperty
	 *            sort property this column represents
	 */
	public FilteredAbstractColumn(IModel displayModel, String sortProperty)
	{
		super(displayModel, sortProperty);
	}

	/**
	 * Constructor
	 * 
	 * @param displayModel
	 *            model used to display the header text of this column
	 */
	public FilteredAbstractColumn(Model displayModel)
	{
		super(displayModel);
	}


}
