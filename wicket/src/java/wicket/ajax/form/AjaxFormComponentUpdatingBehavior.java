package wicket.ajax.form;

import wicket.WicketRuntimeException;
import wicket.ajax.AjaxBehavior;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.FormComponent;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;


public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior
{
	public AjaxFormComponentUpdatingBehavior(String event)
	{
		super(event);
	}

	protected void onBind()
	{
		if (!(getComponent() instanceof FormComponent))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
					+ " can only be added to an isntance of a FormComponent");
		}
	}

	protected void onCheckEvent(String event)
	{
		//TODO check event
	}
	
	protected FormComponent getFormComponent()
	{
		return (FormComponent)getComponent();
	}

	protected String getEventHandler()
	{
		FormComponent fc = getFormComponent();

		String url = getCallbackUrl();
		AppendingStringBuffer buff = new AppendingStringBuffer(url.length() + 64);
		buff.append("'");
		buff.append(url);
		buff.append("&");
		buff.append(fc.getInputName());
		buff.append("='+");
		buff.append("wicketGetValue(this)");

		return buildAjaxCallRaw(buff.toString());
	}

	protected final void onEvent(AjaxRequestTarget target)
	{
		FormComponent fc = getFormComponent();
		fc.registerNewUserInput();
		fc.validate();
		if (fc.hasErrorMessage())
		{
			fc.invalid();
		}
		else
		{
			fc.valid();
			fc.updateModel();
			// TODO Ajax: do we need to persist values for persistent components
		}

		onUpdate(target);
	}

	protected abstract void onUpdate(AjaxRequestTarget target);


}
