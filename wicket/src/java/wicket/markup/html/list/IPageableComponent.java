package wicket.markup.html.list;

import wicket.Page;

/**
 * Interface that exposes paging functionality of components that contain
 * multiple pages
 * 
 */
public interface IPageableComponent
{
	/**
	 * Returns the current page index of the component
	 * 
	 * @return current page index
	 */
	int getCurrentPage();

	/**
	 * Sets the current page index of the component
	 * 
	 * @param index
	 *            new current page index
	 */
	void setCurrentPage(int index);

	/**
	 * Returns the number of pages in the component
	 * 
	 * @return number of pages
	 */
	int getPageCount();

	/**
	 * Returns the total number of items contained in the component
	 * 
	 * @return number of items
	 */
	int getItemCount();

	/**
	 * Returns the Page on which the component resides
	 * 
	 * This method does not usually need to be implemented or overridden because
	 * the component already provides this method
	 * 
	 * @return wicket page
	 */
	Page getPage();
}
