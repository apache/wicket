package wicket.examples.compref;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * RadioGroup and Radio components example page
 * 
 * @author ivaynberg
 */
public class RadioGroupPage extends WicketExamplePage
{
	private final RadioGroup group;
	
	/**
	 * Constructor
	 */
	public RadioGroupPage()
	{
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("selected person: " + RadioGroupPage.this.group.getModelObjectAsString());
			}
		};
		this.group = new RadioGroup(form, "group", new Model());

		new ListView<Person>(group, "persons", ComponentReferenceApplication.getPersons())
		{
			@Override
			protected void populateItem(final ListItem item)
			{
				new Radio(item, "radio", item.getModel());
				new Label(item, "name", new PropertyModel(item.getModel(), "name"));
				new Label(item, "lastName", new PropertyModel(item.getModel(), "lastName"));
			}
		};

		new FeedbackPanel(this, "feedback");
	}

	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n" + "<span wicket:id=\"group\">\n"
				+ "<tr wicket:id=\"persons\">\n"
				+ "<td><input type=\"radio\" wicket:id=\"radio\"/></td>\n"
				+ "<td><span wicket:id=\"name\">[this is where name will be]</span></td>\n"
				+ "<td><span wicket:id=\"lastName\">[this is where lastname will be]</span></td>\n"
				+ "</tr>\n" + "</span>" + "</form>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;RadioGroup group=new RadioGroup(\"group\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(group);<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListView persons=new ListView(\"persons\", getPersons()) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;protected void populateItem(ListItem item) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Radio(\"radio\", item.getModel()));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"name\", new PropertyModel(item.getModel(), \"name\")));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"lastName\", new PropertyModel(item.getModel(), \"lastName\")));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;};<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;group.add(persons);<br/>";
		new ExplainPanel(this, html, code);
	}
}
