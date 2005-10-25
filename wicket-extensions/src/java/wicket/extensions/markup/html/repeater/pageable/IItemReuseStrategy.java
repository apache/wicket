package wicket.extensions.markup.html.repeater.pageable;

import java.util.Iterator;

/**
 * Interface for item reuse strategies.
 * <p>
 * <u>Notice:</u> Child items will be rendered in the order they are provided
 * by the returned iterator, so it is important that the strategy preserve the
 * order of the
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public interface IItemReuseStrategy
{

	/**
	 * Returns an iterator over items that will be added to the view. The
	 * iterator needs to return all the items because the old ones are removed
	 * prior to the new ones added.
	 * 
	 * @param factory
	 *            implementation of IItemFactory
	 * @param newModels
	 *            iterator over models for items
	 * @param existingItems
	 *            iterator over child items
	 * @return iterator over items that will be added after all the old items
	 *         are moved.
	 */
	Iterator getItems(IItemFactory factory, Iterator newModels, Iterator existingItems);
}
