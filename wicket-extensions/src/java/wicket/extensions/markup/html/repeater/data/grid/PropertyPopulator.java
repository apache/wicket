package wicket.extensions.markup.html.repeater.data.grid;

import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;
import wicket.model.PropertyModel;

public class PropertyPopulator implements ICellPopulator
{
	private static final long serialVersionUID = 1L;
	private String property;
	
	public PropertyPopulator(String property) {
		//TODO if property==null throw new illegalarg
		this.property=property;
	}
	public void populateItem(Item cellItem, String componentId, IModel rowModel)
	{
		cellItem.add(new Label(componentId, new PropertyModel(rowModel, property)));
	}

}
