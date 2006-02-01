package wicket.quickstart.partial;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.FormComponent;

public abstract class FormComponentAjaxBehavior extends AjaxBehavior
{

	//FIXME add onbind to abstract ajax behaviors so components can check what tag they are attached to
	
	protected FormComponent getFormComponent() {
		return (FormComponent)getComponent();
	}
	

}
