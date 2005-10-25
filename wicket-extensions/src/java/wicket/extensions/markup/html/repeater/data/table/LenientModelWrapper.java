package wicket.extensions.markup.html.repeater.data.table;

import wicket.Component;
import wicket.model.IModel;

/**
 * model wrapper that returns the specified default value if the model object is
 * null
 */
public class LenientModelWrapper implements IModel
{
	private static final long serialVersionUID = 1L;

	private IModel model;
	private Object defaultValue;

	/**
	 * @param model
	 *            model to be wrapped
	 * @param defaultValue
	 *            default value to be returned if model object is null
	 */
	public LenientModelWrapper(IModel model, Object defaultValue)
	{
		this.model = model;
		this.defaultValue = defaultValue;
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public IModel getNestedModel()
	{
		return model.getNestedModel();
	}

	/**
	 * @see wicket.model.IModel#getObject(wicket.Component)
	 */
	public Object getObject(Component component)
	{
		try
		{
			return model.getObject(component);
		}
		catch (RuntimeException e)
		{
			return defaultValue;
		}
		catch (Exception e)
		{
			return defaultValue;
		}
	}

	/**
	 * @see wicket.model.IModel#setObject(wicket.Component, Object)
	 */
	public void setObject(Component component, Object object)
	{
		model.setObject(component, object);
	}

	/**
	 * @see wicket.model.IModel#detach()
	 */
	public void detach()
	{
		model.detach();
	}

}
