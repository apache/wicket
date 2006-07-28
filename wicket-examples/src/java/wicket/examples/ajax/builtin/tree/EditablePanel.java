package wicket.examples.ajax.builtin.tree;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Panel that contains an text field that submits automatically after it loses focus.
 *  
 * @author Matej Knopp
 */
public class EditablePanel extends Panel {

	/**
	 * Panel constructor.
	 * 
	 * @param id 
	 * 			Markup id
	 * 
	 * @param inputModel
	 * 			Model of the text field
	 */
	public EditablePanel(String id, IModel inputModel) {
		super(id);
		
		TextField field = new TextField("textfield", inputModel);
		add(field);
		
		field.add(new AjaxFormComponentUpdatingBehavior("onblur") 
		{
			protected void onUpdate(AjaxRequestTarget target) 
			{
			}
		});
	}

}
