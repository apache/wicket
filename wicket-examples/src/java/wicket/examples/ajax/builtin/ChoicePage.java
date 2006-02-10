package wicket.examples.ajax.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wicket.Component;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.behavior.MarkupIdSetter;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Linked select boxes example
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class ChoicePage extends BasePage
{

	private String selectedMake;

	private Map modelsMap = new HashMap(); // map:company->model

	/**
	 * @return Currently selected make
	 */
	public String getSelectedMake()
	{
		return selectedMake;
	}

	/**
	 * @param selectedMake The make that is currently selected
	 */
	public void setSelectedMake(String selectedMake)
	{
		this.selectedMake = selectedMake;
	}


	/**
	 * Constructor
	 * 
	 */
	public ChoicePage()
	{
		modelsMap.put("AUDI", Arrays.asList(new String[] { "A4", "A6", "TT" }));
		modelsMap.put("CADILLAC", Arrays.asList(new String[] { "CTS", "DTS", "ESCALADE", "SRX",
				"DEVILLE" }));
		modelsMap.put("FORD", Arrays.asList(new String[] { "CROWN", "ESCAPE", "EXPEDITION",
				"EXPLORER", "F-150" }));

		IModel makeChoices = new AbstractReadOnlyModel()
		{

			public Object getObject(Component component)
			{
				Set keys = modelsMap.keySet();
				List list = new ArrayList(keys.size());
				list.addAll(keys);
				return list;
			}

		};

		IModel modelChoices = new AbstractReadOnlyModel()
		{

			public Object getObject(Component component)
			{
				List models = (List)modelsMap.get(selectedMake);
				if (models == null)
				{
					models = Collections.EMPTY_LIST;
				}
				return models;
			}

		};

		Form form = new Form("form");
		add(form);

		final DropDownChoice makes = new DropDownChoice("makes", new PropertyModel(this,
				"selectedMake"), makeChoices);

		/*
		 * we put the second drop down into a simple webmarkup container which
		 * will be represented as a span. we do this because internet explorer
		 * does not allow us to call selectbox.innerHtml='foo' to update the
		 * componnet, so instead we update the span that contains the select
		 * box.
		 * 
		 * same trick can be used when a listview needs to be rerendered.
		 */
		final WebMarkupContainer modelsContainer = new WebMarkupContainer("modelsContainer");
		modelsContainer.add(MarkupIdSetter.INSTANCE);

		final DropDownChoice models = new DropDownChoice("models", new Model(), modelChoices);

		form.add(makes);
		form.add(modelsContainer);
		modelsContainer.add(models);


		makes.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{

			protected void onUpdate(AjaxRequestTarget target)
			{
				target.addComponent(modelsContainer);

			}
		});
	}

}
