package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.TextField;
import wicket.model.IModel;

public class TextFilter extends AbstractFilter
{
	private static final long serialVersionUID = 1L;
	
	private final TextField filter;
	
	public TextFilter(String id, IModel model, FilterForm form)
	{
		super(id);
		filter=new TextField("filter", model);
		form.addCssId(filter);
		form.addFocusRecorder(filter);
		add(filter);
	}

	public final TextField getFilter() {
		return filter;
	}

	
	
}
