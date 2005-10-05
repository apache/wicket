/**
 * 
 */
package wicket.markup.html.navigation.paging;

/**
 * Components that implement this interface will be pageable, they should return
 * the pagecount so that an object/component knows how many pages it can use for
 * the setCurrentPage method.
 * 
 * The PageableListView is one example that is Pageable. But also a Form could be 
 * pageable so that you can scroll to sets of records that you display in that form
 * with any navigator you want.
 * 
 * @author jcompagner
 *
 */
public interface IPageable
{
	/**
	 * @return The current page that is or will be rendered rendered.
	 */
	public int getCurrentPage();
	
	/**
	 * Sets the a page that should be rendered.
	 *  
	 * @param page The page that should be rendered. 
	 */
	public void setCurrentPage(int page);
	
	/**
	 * Gets the total number of pages this pageable object has.
	 * 
	 * @return The total number of pages this pageable object has 
	 */
	public int getPageCount();
}
