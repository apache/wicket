package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import wicket.markup.html.form.Form;
import wicket.model.IModel;

public abstract class FilteredPropertyColumn extends PropertyColumn implements IFilteredColumn
{
	private static final long serialVersionUID = 1L;

	public FilteredPropertyColumn(IModel displayModel, String sortProperty, String ognlExpression)
	{
		super(displayModel, sortProperty, ognlExpression);
		// TODO Auto-generated constructor stub
	}

	public FilteredPropertyColumn(IModel displayModel, String ognlExpression)
	{
		super(displayModel, ognlExpression);
		// TODO Auto-generated constructor stub
	}

}
