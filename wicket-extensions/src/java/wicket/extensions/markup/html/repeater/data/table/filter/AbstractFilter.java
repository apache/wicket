package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.FormComponent;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Base class for filters that provides some useful functionality
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AbstractFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	private FilterForm form;

	/**
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 */
	public AbstractFilter(String id, FilterForm form)
	{
		super(id);
		this.form = form;
	}

	/**
	 * Enables the tracking of focus for the specified form component. This
	 * allows the filter form to restore focus to the component which caused the
	 * form submission. Great for when you are inside a filter textbox and use
	 * the enter key to submit the filter.
	 * 
	 * @param fc
	 *            form component for which focus tracking will be enabled
	 */
	protected void enableFocusTracking(FormComponent fc)
	{
		form.enableFocusTracking(fc);
	}

	protected IFilterStateLocator getStateLocator()
	{
		return form.getStateLocator();
	}

	protected IModel getStateModel()
	{
		return form.getModel();
	}

	protected Object getState()
	{
		return form.getModelObject();
	}

}
