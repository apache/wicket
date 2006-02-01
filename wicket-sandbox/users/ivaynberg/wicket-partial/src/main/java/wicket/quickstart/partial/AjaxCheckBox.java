package wicket.quickstart.partial;

import wicket.markup.html.form.CheckBox;
import wicket.model.IModel;

public abstract class AjaxCheckBox extends CheckBox
{
	public AjaxCheckBox(String id)
	{
		this(id, null);
	}

	public AjaxCheckBox(String id, IModel model)
	{
		super(id, model);

		add(new CheckBoxAjaxBehavior()
		{

			protected void respond(AjaxRequestTarget target, boolean checked)
			{
				onClick(target, checked);
			}

		});
	}

	protected abstract void onClick(AjaxRequestTarget target, boolean checked);

}
