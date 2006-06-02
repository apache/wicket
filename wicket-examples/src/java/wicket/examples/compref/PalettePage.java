package wicket.examples.compref;

import java.util.ArrayList;
import java.util.List;

import wicket.examples.WicketExamplePage;
import wicket.extensions.markup.html.form.palette.Palette;
import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;

/**
 * Palette component example
 * 
 * @author ivaynberg
 */
public class PalettePage extends WicketExamplePage
{
	private final Palette palette;
	
	/**
	 * Constructor
	 */
	public PalettePage()
	{
		Form form = new Form(this, "form")
		{
			@Override
			protected void onSubmit()
			{
				info("selected person(s): " + palette.getModelObjectAsString());
			}
		};

		new FeedbackPanel(this, "feedback");
		
		List<Person> persons = ComponentReferenceApplication.getPersons();
		IChoiceRenderer renderer = new ChoiceRenderer("fullName", "fullName");

		this.palette = new Palette(form, "palette", new Model<List<Person>>(new ArrayList<Person>()), 
				new Model<List<Person>>(persons), renderer, 10, true);
	}

	@Override
	protected void explain()
	{
		String html = "<form wicket:id=\"form\">\n" + "<span wicket:id=\"palette\">\n"
				+ "</span>\n</form>";
		String code = "&nbsp;&nbsp;&nbsp;&nbsp;Form f=new Form(\"form\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;add(f);<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;List persons = ComponentReferenceApplication.getPersons();;<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;IChoiceRenderer renderer = new ChoiceRenderer(\"fullName\", \"fullName\");<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;final Palette palette = new Palette(\"palette\", new Model(new ArrayList()), new Model(<br/>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Serializable)persons), renderer, 10, true);<br/>";
		new ExplainPanel(this, html, code);
	}
}
