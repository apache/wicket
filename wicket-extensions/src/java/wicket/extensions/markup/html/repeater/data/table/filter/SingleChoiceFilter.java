package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

public class SingleChoiceFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	private final DropDownChoice choice;
	
	public SingleChoiceFilter(String id, IModel model, IModel choices, IChoiceRenderer renderer)
	{
		super(id, model);

		choice=new DropDownChoice("filter", model, choices, renderer);
		add(choice);
	}
	
	public DropDownChoice getChoice() {
		return choice;
	}
}
