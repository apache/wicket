package wicket.quickstart.partial;

import wicket.markup.ComponentTag;
import wicket.util.string.AppendingStringBuffer;

public abstract class CheckBoxAjaxBehavior extends FormComponentAjaxBehavior
{

	public void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		AppendingStringBuffer onclick = new AppendingStringBuffer(128);

		onclick.append("wicketAjaxGet('").append(getCallbackUrl());
		onclick.append("&").append(getFormComponent().getInputName()).append(
				"='+this.checked)");

		tag.put("onclick", onclick.toString());
	}

	protected void respond(AjaxRequestTarget target)
	{
		getFormComponent().updateModel();
		respond(target, ((Boolean) getFormComponent().getModelObject()).booleanValue());
	}

	protected abstract void respond(AjaxRequestTarget target, boolean checked);

}
