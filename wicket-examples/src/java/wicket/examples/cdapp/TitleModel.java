package wicket.examples.cdapp;

import wicket.Component;
import wicket.contrib.data.model.PersistentObjectModel;
import wicket.examples.cdapp.model.CD;
import wicket.model.AbstractDetachableModel;

/**
 * Special model for the title header. It returns the CD title if there's a
 * loaded object (when the id != null) or it returns a special string in case
 * there is no loaded object (if id == null).
 */
public class TitleModel extends AbstractDetachableModel
{
	/** decorated model; provides the current id. */
	private final PersistentObjectModel cdModel;

	/**
	 * Construct.
	 * 
	 * @param cdModel the model to decorate
	 */
	public TitleModel(PersistentObjectModel cdModel)
	{
		this.cdModel = cdModel;
	}

	/**
	 * @see AbstractDetachableModel#onSetObject(Component, Object)
	 */
	public void onSetObject(final Component component, final Object object)
	{
		cdModel.setObject(component, object);
	}

	/**
	 * @see AbstractDetachableModel#onAttach()
	 */
	protected void onAttach()
	{
		cdModel.attach();
	}

	/**
	 * @see AbstractDetachableModel#onDetach()
	 */
	protected void onDetach()
	{
		cdModel.detach();
	}

	/**
	 * @see AbstractDetachableModel#onGetObject(Component)
	 */
	protected Object onGetObject(final Component component)
	{
		if (cdModel.getId() != null) // it is allready persistent
		{
			CD cd = (CD)cdModel.getObject(component);
			return cd.getTitle();
		}
		else // it is a new cd
		{
			return "<NEW CD>";
		}
	}

	/**
	 * @see wicket.model.IModel#getNestedModel()
	 */
	public Object getNestedModel()
	{
		return cdModel;
	}
}