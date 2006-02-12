package wicket.examples.ajax.builtin;

import wicket.ajax.form.AjaxFormValidatingBehavior;
import wicket.behavior.MarkupIdSetter;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.markup.html.form.validation.LengthValidator;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is trigger in
 * onblur javascript event handler in every form input.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class FormPage extends BasePage
{

	private Bean bean = new Bean();

	/**
	 * Constructor
	 */
	public FormPage()
	{

		// add feedback panel with a markup id setter so it can be updated via
		// ajax
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback.add(MarkupIdSetter.INSTANCE));

		// add form with markup id setter so it can be updated via ajax
		Form form = new Form("form", new CompoundPropertyModel(bean));
		add(form);
		form.add(MarkupIdSetter.INSTANCE);

		FormComponent fc;

		// add form components to the form as usual

		fc = new RequiredTextField("name");
		fc.add(LengthValidator.min(4));

		form.add(fc);

		fc = new RequiredTextField("email");
		fc.add(EmailAddressPatternValidator.getInstance());

		form.add(fc);

		// add ajax form validating behavior to onblur event of all form
		// components
		AjaxFormValidatingBehavior.addToAllFormComponents(form, "onblur");

	}

	public static class Bean
	{
		private String name, email;

		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
	}


}
