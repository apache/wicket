package wicket.ajax.markup.html.form;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.markup.html.form.CheckBox;
import wicket.model.IModel;

public abstract class AjaxCheckBox extends CheckBox 
{
	private static final long serialVersionUID = 1L;

	public AjaxCheckBox(String id)
	{
		this(id, null);
	}

	public AjaxCheckBox(String id, IModel model)
	{
		super(id, model);
		add(new AjaxFormComponentUpdatingBehavior("oncheck") {

			private static final long serialVersionUID = 1L;

			protected void onUpdate(AjaxRequestTarget target)
			{
				AjaxCheckBox.this.onUpdate(target);
			}
			
		});
	}

	protected abstract void onUpdate(AjaxRequestTarget target);
	

}
