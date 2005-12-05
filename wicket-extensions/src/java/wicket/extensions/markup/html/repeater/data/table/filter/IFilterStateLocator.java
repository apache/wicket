package wicket.extensions.markup.html.repeater.data.table.filter;

public interface IFilterStateLocator
{
	Object getFilterState();
	void setFilterState(Object state);
}
