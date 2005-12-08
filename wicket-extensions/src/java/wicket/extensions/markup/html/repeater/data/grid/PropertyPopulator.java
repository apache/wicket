package wicket.extensions.markup.html.repeater.data.grid;

import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;
import wicket.model.PropertyModel;

/**
 * A convinience implementation of {@link ICellPopulator} that adds a label that
 * will display the value of the specified property. Non-string properties will
 * be converted to a string before display.
 * <p>
 * Example
 * 
 * <pre>
 * ICellPopulator cityPopulator = new PropertyPopulator(&quot;address.city&quot;);
 * </pre>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class PropertyPopulator implements ICellPopulator
{
	private static final long serialVersionUID = 1L;
	private String property;

	/**
	 * Constructor
	 * 
	 * @param property
	 *            property whose value will be displayed in the cell. uses
	 *            wicket's {@link PropertyModel} notation.
	 */
	public PropertyPopulator(String property)
	{
		if (property == null)
		{
			throw new IllegalArgumentException("argument [property] cannot be null");
		}
		this.property = property;
	}

	/**
	 * @see wicket.extensions.markup.html.repeater.data.grid.ICellPopulator#populateItem(wicket.extensions.markup.html.repeater.refreshing.Item,
	 *      java.lang.String, wicket.model.IModel)
	 */
	public void populateItem(Item cellItem, String componentId, IModel rowModel)
	{
		cellItem.add(new Label(componentId, new PropertyModel(rowModel, property)));
	}

}
