package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.AbstractBehaviour;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.HiddenField;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.Model;

public class FilterForm extends Form
{
	private static final long serialVersionUID = 1L;

	private final HiddenField hidden;
	private final IFilterStateLocator locator;
	
	public FilterForm(String id, IFilterStateLocator locator)
	{
		super(id, new FilterStateModel(locator));
		this.locator=locator;
		hidden=new HiddenField("hidden", new Model());
		addCssId(hidden);
		add(hidden);
	}
	
	public final String getHiddenInputName() {
		return hidden.getInputName();
	}
	public final String getHiddenInputCssId() {
		return hidden.getPageRelativePath();
	}
	public final IFilterStateLocator getLocator() {
		return locator;
	}
	
	public String getCssId(FormComponent fc) {
		return fc.getPageRelativePath();
	}
	
	public void addCssId(FormComponent fc) {
		fc.add(new AbstractBehaviour() {
			private static final long serialVersionUID = 1L;
			
			public void onComponentTag(Component component, ComponentTag tag)
			{
				tag.put("id", component.getPageRelativePath());
				super.onComponentTag(component, tag);
			}
		});
	}

	public String getFocusHandler(FormComponent fc) {
		return ("_filter_focus(this, '"+getHiddenInputName()+"');");
	}
	
	public void addFocusRecorder(FormComponent fc) {
		fc.add(new AttributeModifier("onfocus", true, new Model(getFocusHandler(fc))));
	}

}
