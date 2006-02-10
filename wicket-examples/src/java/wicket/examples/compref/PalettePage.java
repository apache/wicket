package wicket.examples.compref;

import java.io.Serializable;
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
	/**
	 * Constructor
	 */
	public PalettePage()
	{
		List persons = ComponentReferenceApplication.getPersons();
		IChoiceRenderer renderer = new ChoiceRenderer("fullName", "fullName");

		final Palette palette = new Palette("palette", new Model(new ArrayList()), new Model(
				(Serializable)persons), renderer, 10, true);


		Form form = new Form("form")
		{
			protected void onSubmit()
			{
				info("selected person(s): " + palette.getModelObjectAsString());
			}
		};

		add(form);
		form.add(palette);

		add(new FeedbackPanel("feedback"));
	}

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
		add(new ExplainPanel(html, code));
	}
}
