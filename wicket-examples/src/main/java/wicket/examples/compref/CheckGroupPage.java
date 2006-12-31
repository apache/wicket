package wicket.examples.compref;

import java.util.ArrayList;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Check;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.CheckGroupSelector;
import wicket.markup.html.form.Form;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.PropertyModel;

/**
 * CheckGroup and Check components example page
 * 
 * @author ivaynberg
 */
public class CheckGroupPage extends WicketExamplePage
{
	private final CheckGroup group;
	
	/**
	 * Constructor
	 */
	public CheckGroupPage()
	{
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("selected person(s): " + group.getModelObjectAsString());
			}
		};

		group = new CheckGroup<String>(form, "group", new ArrayList<String>());

		new CheckGroupSelector(group, "groupselector");
		new ListView<Person>(group, "persons", ComponentReferenceApplication.getPersons())
		{

			@Override
			protected void populateItem(final ListItem<Person> item)
			{
				new Check<Person>(item, "checkbox", item.getModel());
				new Label(item, "name", new PropertyModel(item.getModel(), "name"));
				new Label(item, "lastName", new PropertyModel(item.getModel(), "lastName"));
			}

		};

		new FeedbackPanel(this, "feedback");
	}

	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n"
				+ "<span wicket:id=\"group\">\n"
				+ "<input type=\"checkbox\" wicket:id=\"groupselector\">check/uncheck all</input>\n"
				+ "<tr wicket:id=\"persons\">\n"
				+ "<td><input type=\"checkbox\" wicket:id=\"checkbox\"/></td>\n"
				+ "<td><span wicket:id=\"name\">[this is where name will be]</span></td>\n"
				+ "<td><span wicket:id=\"lastName\">[this is where lastname will be]</span></td>\n"
				+ "</tr>\n</span>\n</form>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;CheckGroup group=new CheckGroup(\"group\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;form.add(group);<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;group.add(new CheckGroupSelector(\"groupselector\"));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;ListView persons=new ListView(\"persons\", getPersons()) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;protected void populateItem(ListItem item) {<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Check(\"check\", item.getModel()));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"name\", new PropertyModel(item.getModel(), \"name\")));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;item.add(new Label(\"lastName\", new PropertyModel(item.getModel(), \"lastName\")));<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;};<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;group.add(persons);<br/>";
		new ExplainPanel(this, html, code);
	}
}
