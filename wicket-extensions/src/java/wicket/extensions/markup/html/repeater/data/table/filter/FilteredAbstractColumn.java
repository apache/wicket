package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.model.IModel;
import wicket.model.Model;

public abstract class FilteredAbstractColumn extends AbstractColumn implements IFilteredColumn
{
	private static final long serialVersionUID = 1L;

	public FilteredAbstractColumn(IModel displayModel, String sortProperty)
	{
		super(displayModel, sortProperty);
		// TODO Auto-generated constructor stub
	}

	public FilteredAbstractColumn(Model displayModel)
	{
		super(displayModel);
		// TODO Auto-generated constructor stub
	}


}
