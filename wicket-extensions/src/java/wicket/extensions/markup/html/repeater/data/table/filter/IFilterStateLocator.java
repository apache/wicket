package wicket.extensions.markup.html.repeater.data.table.filter;

import java.io.Serializable;

public interface IFilterStateLocator extends Serializable
{
	Object getFilterState();
	void setFilterState(Object state);
}
