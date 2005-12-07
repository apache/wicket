package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.Component;
import wicket.model.AbstractModel;
import wicket.model.IModel;

/**
 * Model that wraps filter state locator to make its use transparent to wicket
 * components.
 * <p>
 * Example:
 * 
 * <pre>
 * IFilterStateLocator locator = getLocator();
 * TextField tf = new TextField(&quot;tf&quot;, new FilterStateModel(locator));
 * </pre>
 * 
 * Text field tf will now user the object that filter state locator locates as
 * its underlying model.
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FilterStateModel extends AbstractModel
{
	private static final long serialVersionUID = 1L;

	private IFilterStateLocator locator;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            IFilterStateLocator implementation used to provide model
	 *            object for this model
	 */
	public FilterStateModel(IFilterStateLocator locator)
	{
		this.locator = locator;
	}

	/**
	 * @return the underlying IFilterStateLocator
	 */
	public IFilterStateLocator getLocator() {
		return locator;
	}
	
	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return null;
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		return locator.getFilterState();
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, java.lang.Object)
	 */
	public void setObject(Component component, Object object)
	{
		locator.setFilterState(object);
	}

}
