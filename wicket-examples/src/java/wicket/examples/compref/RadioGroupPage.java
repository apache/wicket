package wicket.examples.compref;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.PropertyModel;

/**
 * RadioGroup and Radio components example page
 * @author ivaynberg
 */
public class RadioGroupPage extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public RadioGroupPage() {
		
		final RadioGroup group=new RadioGroup("group");

		Form form=new Form("form") {
			protected void onSubmit() {
				info("selected person: "+group.getModelObjectAsString());
			}
		};
		
		add(form);
		form.add(group);
		
		ListView persons=new ListView("persons", ComponentReferenceApplication.getPersons()) {

			protected void populateItem(ListItem item)
			{
				item.add(new Radio("radio", item.getModel()));
				item.add(new Label("name", new PropertyModel(item.getModel(), "name")));
				item.add(new Label("lastName", new PropertyModel(item.getModel(), "lastName")));
			}
			
		};
		
		group.add(persons);
		
		add(new FeedbackPanel("feedback"));
	}
	
	protected void explain()
	{
		String html="<form wicket:id=\"form\">\n"
			+"<span wicket:id=\"radioGroup\">\n"
			+"<input type=\"radio\" wicket:id=\"radio1\">radio choice 1</input>\n"
			+"<input type=\"radio\" wicket:id=\"radio2\">radio choice 2</input>\n"
			+"</span>\n</form>";
		String code="&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
				+"&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
				+"&nbsp;&nbsp;&nbsp;&nbsp;RadioGroup group=new RadioGroup(\"group\", new PropertyModel(bean, \"value\"));<br/>"
				+"&nbsp;&nbsp;&nbsp;&nbsp;form.add(group);<br/>"
				+"&nbsp;&nbsp;&nbsp;&nbsp;group.add(new Radio(\"radio1\", new Model(\"value1\")));<br/>"
				+"&nbsp;&nbsp;&nbsp;&nbsp;group.add(new Radio(\"radio2\", new Model(\"value2\")));<br/>";
		add(new ExplainPanel(html, code));
	}
}
