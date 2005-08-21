/**
 * 
 */
package wicket.markup.html.navigation.paging;

/**
 * This interface is used by the PagingNavigator components to get the label 
 * of the pages there are for a IPageable component. By default this is only 
 * the page number.<br>
 * <br>
 * This interface can be used to override this for example into 1-10,11-20,20-24:<br>
 * <br>
 * final int pageSize = 10;
 * final PageableListView listview = new PageableListView("listview",data,pageSize);<br>
 * IPagingLableProvider labelProvider = new IPagingLabelProvider(){<br>
 *      public String getPageLabel(int page)<br>
 *      {<br>
 *      	int size = listview.getList().size();<br>
 *      	int current = page*pageSize;<br>
 *      	int end = current+pageSize;<br>
 *          if(end > size) end = size;<br>
 *      	current++; // page start at 0.<br>
 *      	return current + "-" + end;<br>
 *      }<br>
 * }<br>
 * PagingNavigator navigator = new PagingNavigator("navigator", listview,labelProvider);<br>
 * 
 * @author jcompagner
 *
 */
public interface IPagingLabelProvider
{
	
	/**
	 * @param page The page number for which the label must be generated.
	 * @return The string to be displayed for this page number
	 */
	public String getPageLabel(int page);
}
