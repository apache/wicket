package wicket.extensions.markup.html.repeater.pageable;

import wicket.model.IModel;

/**
 * This class is here only to eliminate compiler errors because it has been
 * relocated to wicket.extensions.markup.html.repeater.refreshing.Item
 * 
 * @see wicket.extensions.markup.html.repeater.refreshing.Item
 * @deprecated
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class Item extends wicket.extensions.markup.html.repeater.refreshing.Item
{
	/**
	 * @param id
	 * @param index
	 * @param model
	 */
	public Item(String id, int index, IModel model)
	{
		super(id, index, model);
	}

	private static final long serialVersionUID = 1L;


}
