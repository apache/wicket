package wicket.extensions.markup.html.repeater.pageable;

import wicket.model.IModel;

/**
 * Factory interface for creating new child item containers for <b>AbstractPageableView</b>.
 * 
 * @see wicket.extensions.markup.html.repeater.pageable.AbstractPageableView
 * 
 * @author igor
 * 
 */
public interface IItemFactory
{
	/**
	 * Factory method for instances of Item. Each generated item must have a
	 * unique id with respect to other generated items.
	 * 
	 * @param index
	 *            the index of the new data item
	 * @param model
	 *            the model for the new data item
	 * 
	 * @return DataItem new DataItem
	 */
	Item newItem(final int index, final IModel model);

}
