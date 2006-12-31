package wicket.examples.ajax.builtin.tree;

import wicket.MarkupContainer;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
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
	 * @param parent
	 * 			Parent of this component
	 * 
	 * @param id 
	 * 			Markup id
	 * 
	 * @param inputModel
	 * 			Model of the text field
	 */
	public EditablePanel(final MarkupContainer parent, final String id, final IModel<String> inputModel) {
		super(parent, id);
		
		TextField<String> field = new TextField<String>(this, "textfield", inputModel);
		
		field.add(new AjaxFormComponentUpdatingBehavior(ClientEvent.BLUR) 
		{
			@Override
			protected void onUpdate(final AjaxRequestTarget target) 
			{
			}
		});
	}

}
