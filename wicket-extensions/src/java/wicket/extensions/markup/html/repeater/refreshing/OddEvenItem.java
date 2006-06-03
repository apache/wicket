package wicket.extensions.markup.html.repeater.refreshing;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Item that sets class="even" or class="odd" attributes based on its index
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class OddEvenItem extends Item
{
	private static final long serialVersionUID = 1L;

	private String CLASS_EVEN = "even";
	private String CLASS_ODD = "odd";

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param index
	 *            item index
	 * @param model
	 *            item model
	 */
	public OddEvenItem(MarkupContainer parent, final String id, int index, IModel model)
	{
		super(parent, id, index, model);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("class", (getIndex() % 2 == 0) ? CLASS_EVEN : CLASS_ODD);
	}

}
