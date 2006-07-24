package app;

import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

public class EditablePanel extends Panel {

	public EditablePanel(MarkupContainer parent, String id, IModel<String> inputModel) {
		super(parent, id);
		
		TextField field = new TextField<String>(this, "textfield", inputModel);
		
		field.add(new AjaxFormComponentUpdatingBehavior(ClientEvent.BLUR) {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
			}
		});
	}

}
