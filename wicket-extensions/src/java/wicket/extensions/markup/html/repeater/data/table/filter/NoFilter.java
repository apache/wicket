package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.panel.Panel;

/**
 * Component used to represent a filter component when no filter is provided.
 * This component generates a blank space.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class NoFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 *            component id
	 */
	public NoFilter(String id)
	{
		super(id);
	}
}
