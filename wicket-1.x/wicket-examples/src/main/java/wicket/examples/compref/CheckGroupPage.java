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
	/**
	 * Constructor
	 */
	public CheckGroupPage()
	{

		final CheckGroup group = new CheckGroup("group", new ArrayList());
		Form form = new Form("form")
		{
			protected void onSubmit()
			{
				info("selected person(s): " + group.getModelObjectAsString());
			}
		};

		add(form);
		form.add(group);
		group.add(new CheckGroupSelector("groupselector"));
		ListView persons = new ListView("persons", ComponentReferenceApplication.getPersons())
		{

			protected void populateItem(ListItem item)
			{
				item.add(new Check("checkbox", item.getModel()));
				item.add(new Label("name", new PropertyModel(item.getModel(), "name")));
				item.add(new Label("lastName", new PropertyModel(item.getModel(), "lastName")));
			}

		};

		group.add(persons);

		add(new FeedbackPanel("feedback"));
	}

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
		add(new ExplainPanel(html, code));
	}
}
