package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

public class TextFilter extends Panel
{
	private static final long serialVersionUID = 1L;
	
	private final TextField filter;
	
	public TextFilter(String id, IModel model)
	{
		super(id);
		filter=new TextField("filter", model);
		add(filter);
	}

	public final TextField getFilter() {
		return filter;
	}

	
	
}
