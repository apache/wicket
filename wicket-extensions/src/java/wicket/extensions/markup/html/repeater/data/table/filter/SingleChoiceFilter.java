package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.AttributeModifier;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;

public class SingleChoiceFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	private final DropDownChoice choice;

	public SingleChoiceFilter(String id, IModel model, FilterForm form, IModel choices, IChoiceRenderer renderer,
			boolean autoSubmit)
	{
		super(id, model);

		choice = new DropDownChoice("filter", model, choices, renderer);
		if (autoSubmit) {
			choice.add(new AttributeModifier("onchange", true, new Model("this.form.submit();")));
		}
		form.addCssId(choice);
		form.addFocusRecorder(choice);
		add(choice);
	}

	public DropDownChoice getChoice()
	{
		return choice;
	}
}
